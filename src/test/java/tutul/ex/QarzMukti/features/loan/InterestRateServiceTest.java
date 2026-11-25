package tutul.ex.QarzMukti.features.loan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tutul.ex.QarzMukti.shared.common.ResourceNotFoundException;
import tutul.ex.QarzMukti.shared.common.ValidationException;
import tutul.ex.QarzMukti.shared.dto.InterestRateAuditResponse;
import tutul.ex.QarzMukti.shared.dto.LoanInterestRateOverrideRequest;
import tutul.ex.QarzMukti.shared.dto.SystemInterestRateRequest;
import tutul.ex.QarzMukti.shared.dto.SystemInterestRateResponse;
import tutul.ex.QarzMukti.shared.entities.InterestRateAudit;
import tutul.ex.QarzMukti.shared.entities.Loan;
import tutul.ex.QarzMukti.shared.entities.SystemInterestSettings;
import tutul.ex.QarzMukti.shared.entities.User;
import tutul.ex.QarzMukti.shared.repositories.InterestRateAuditRepository;
import tutul.ex.QarzMukti.shared.repositories.LoanRepository;
import tutul.ex.QarzMukti.shared.repositories.SystemInterestSettingsRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for interest rate governance: system-wide rates, per-loan
 * overrides with business-rule validation, and the audit trail.
 */
@ExtendWith(MockitoExtension.class)
class InterestRateServiceTest {

    @Mock
    private SystemInterestSettingsRepository systemInterestSettingsRepository;

    @Mock
    private InterestRateAuditRepository interestRateAuditRepository;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private InterestRateService interestRateService;

    private User admin;
    private Loan loan;

    @BeforeEach
    void setUp() {
        admin = User.builder().username("superadmin").build();

        loan = new Loan();
        loan.setId(UUID.randomUUID());
        loan.setStatus("APPROVED");
        loan.setApprovedInterestRate(new BigDecimal("12.00"));
    }

    @Test
    @DisplayName("Overriding a loan rate updates the loan and writes an audit record")
    void overrideUpdatesLoanAndCreatesAudit() {
        LoanInterestRateOverrideRequest request = LoanInterestRateOverrideRequest.builder()
                .loanId(loan.getId())
                .interestRate(new BigDecimal("15.50"))
                .reason("Promotional rate for repeat borrower")
                .build();

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(interestRateAuditRepository.save(any(InterestRateAudit.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InterestRateAuditResponse response = interestRateService.overrideLoanInterestRate(request, admin);

        assertThat(loan.getApprovedInterestRate()).isEqualByComparingTo("15.50");

        ArgumentCaptor<InterestRateAudit> captor = ArgumentCaptor.forClass(InterestRateAudit.class);
        verify(interestRateAuditRepository).save(captor.capture());
        InterestRateAudit audit = captor.getValue();
        assertThat(audit.getOldInterestRate()).isEqualByComparingTo("12.00");
        assertThat(audit.getNewInterestRate()).isEqualByComparingTo("15.50");
        assertThat(audit.getChangedBy()).isEqualTo(admin);
        assertThat(audit.getChangeReason()).isEqualTo("Promotional rate for repeat borrower");

        assertThat(response.getOldInterestRate()).isEqualByComparingTo("12.00");
        assertThat(response.getNewInterestRate()).isEqualByComparingTo("15.50");
        assertThat(response.getChangedByUsername()).isEqualTo("superadmin");
    }

    @ParameterizedTest
    @ValueSource(strings = {"REJECTED", "FULLY_PAID", "CANCELLED"})
    @DisplayName("Rate override is rejected for terminal loan statuses")
    void overrideRejectedForTerminalStatuses(String status) {
        loan.setStatus(status);
        LoanInterestRateOverrideRequest request = LoanInterestRateOverrideRequest.builder()
                .loanId(loan.getId())
                .interestRate(new BigDecimal("15.00"))
                .reason("should not apply")
                .build();

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> interestRateService.overrideLoanInterestRate(request, admin))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining(status);

        verify(loanRepository, never()).save(any());
        verify(interestRateAuditRepository, never()).save(any());
    }

    @Test
    @DisplayName("Overriding a rate for a missing loan raises ResourceNotFoundException")
    void overrideMissingLoanThrows() {
        UUID missingId = UUID.randomUUID();
        LoanInterestRateOverrideRequest request = LoanInterestRateOverrideRequest.builder()
                .loanId(missingId)
                .interestRate(new BigDecimal("15.00"))
                .build();

        when(loanRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interestRateService.overrideLoanInterestRate(request, admin))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Internal rate lookup falls back to 12% when nothing is configured")
    void currentRateValueFallsBackToDefault() {
        when(systemInterestSettingsRepository.findLatestEffectiveRate(any(Instant.class)))
                .thenReturn(Optional.empty());

        assertThat(interestRateService.getCurrentInterestRateValue())
                .isEqualByComparingTo("12.0");
    }

    @Test
    @DisplayName("Internal rate lookup returns the configured rate when present")
    void currentRateValueReturnsConfiguredRate() {
        SystemInterestSettings settings = new SystemInterestSettings();
        settings.setBaseInterestRate(new BigDecimal("15.00"));

        when(systemInterestSettingsRepository.findLatestEffectiveRate(any(Instant.class)))
                .thenReturn(Optional.of(settings));

        assertThat(interestRateService.getCurrentInterestRateValue())
                .isEqualByComparingTo("15.00");
    }

    @Test
    @DisplayName("Reading the current system rate without configuration raises ResourceNotFoundException")
    void currentSystemRateWithoutConfigurationThrows() {
        when(systemInterestSettingsRepository.findLatestEffectiveRate(any(Instant.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> interestRateService.getCurrentSystemInterestRate())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Setting the system rate persists settings and reports the acting admin")
    void setSystemInterestRatePersistsAndMapsResponse() {
        SystemInterestRateRequest request = SystemInterestRateRequest.builder()
                .interestRate(new BigDecimal("14.00"))
                .reason("Quarterly review")
                .build();

        when(systemInterestSettingsRepository.save(any(SystemInterestSettings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SystemInterestRateResponse response = interestRateService.setSystemInterestRate(request, admin);

        assertThat(response.getBaseInterestRate()).isEqualByComparingTo("14.00");
        assertThat(response.getSetByUsername()).isEqualTo("superadmin");
        assertThat(response.getEffectiveFrom()).isNotNull();
        verify(systemInterestSettingsRepository).save(any(SystemInterestSettings.class));
    }
}
