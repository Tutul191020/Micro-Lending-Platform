package tutul.ex.QarzMukti.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tutul.ex.QarzMukti.common.ResourceNotFoundException;
import tutul.ex.QarzMukti.common.ValidationException;
import tutul.ex.QarzMukti.dto.*;
import tutul.ex.QarzMukti.entities.InterestRateAudit;
import tutul.ex.QarzMukti.entities.Loan;
import tutul.ex.QarzMukti.entities.SystemInterestSettings;
import tutul.ex.QarzMukti.entities.User;
import tutul.ex.QarzMukti.repositories.InterestRateAuditRepository;
import tutul.ex.QarzMukti.repositories.LoanRepository;
import tutul.ex.QarzMukti.repositories.SystemInterestSettingsRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * - System-wide default interest rate (SuperAdmin only)
 * - Per-loan interest rate overrides (SuperAdmin only)
 * - Interest rate audit trail
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterestRateService {

    private final SystemInterestSettingsRepository systemInterestSettingsRepository;
    private final InterestRateAuditRepository interestRateAuditRepository;
    private final LoanRepository loanRepository;

    @Transactional
    public SystemInterestRateResponse setSystemInterestRate(SystemInterestRateRequest request, User admin) {
        log.info("Setting system interest rate to {}% by user: {}", request.getInterestRate(), admin.getUsername());

        // Create new system interest settings
        SystemInterestSettings settings = new SystemInterestSettings();
        settings.setId(UUID.randomUUID());
        settings.setBaseInterestRate(request.getInterestRate());
        settings.setEffectiveFrom(Instant.now());
        settings.setSetByUser(admin);
        settings.setCreatedAt(Instant.now());

        SystemInterestSettings saved = systemInterestSettingsRepository.save(settings);

        log.info("System interest rate set successfully: {}%", saved.getBaseInterestRate());

        return mapToSystemInterestRateResponse(saved);
    }

    /**
     * Get the current active system interest rate
     *
     * @return Current interest rate settings
     */
    @Transactional(readOnly = true)
    public SystemInterestRateResponse getCurrentSystemInterestRate() {
        log.debug("Fetching current system interest rate");

        SystemInterestSettings settings = systemInterestSettingsRepository
                .findLatestEffectiveRate(Instant.now())
                .orElseThrow(() -> new ResourceNotFoundException("No system interest rate configured. Please contact administrator."));

        return mapToSystemInterestRateResponse(settings);
    }

    /**
     * Get the current interest rate as BigDecimal (for internal use)
     *
     * @return Current interest rate value
     */
    @Transactional(readOnly = true)
    public BigDecimal getCurrentInterestRateValue() {
        return systemInterestSettingsRepository
                .findLatestEffectiveRate(Instant.now())
                .map(SystemInterestSettings::getBaseInterestRate)
                .orElse(BigDecimal.valueOf(12.0)); // Default fallback rate
    }

    /**
     * Get paginated history of system interest rate changes
     *
     * @param pageable Pagination parameters
     * @return Page of interest rate history
     */
    @Transactional(readOnly = true)
    public Page<SystemInterestRateResponse> getSystemInterestRateHistory(Pageable pageable) {
        log.debug("Fetching system interest rate history");

        return systemInterestSettingsRepository
                .findAllByOrderByEffectiveFromDesc(pageable)
                .map(this::mapToSystemInterestRateResponse);
    }

    /**
     * Override interest rate for a specific loan (SuperAdmin only)
     * This creates an audit trail entry
     *
     * @param request Request containing loan ID, new rate, and reason
     * @param admin   The SuperAdmin performing the override
     * @return Audit record of the change
     */
    @Transactional
    public InterestRateAuditResponse overrideLoanInterestRate(LoanInterestRateOverrideRequest request, User admin) {
        log.info("Overriding interest rate for loan {} to {}% by user: {}", 
                request.getLoanId(), request.getInterestRate(), admin.getUsername());

        // Find the loan
        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + request.getLoanId()));

        // Validate loan status - can only override for pending or approved loans
        if ( "REJECTED".equals(loan.getStatus()) || "FULLY_PAID".equals(loan.getStatus()) || "CANCELLED".equals(loan.getStatus())) {
            throw new ValidationException("Cannot override interest rate for loan in status: " + loan.getStatus());
        }

        BigDecimal oldRate = loan.getApprovedInterestRate();
        BigDecimal newRate = request.getInterestRate();

        // Update loan interest rate
        loan.setApprovedInterestRate(newRate);
        loanRepository.save(loan);

        // Create audit record
        InterestRateAudit audit = new InterestRateAudit();
        audit.setId(UUID.randomUUID());
        audit.setLoan(loan);
        audit.setOldInterestRate(oldRate);
        audit.setNewInterestRate(newRate);
        audit.setChangedBy(admin);
        audit.setChangeReason(request.getReason());
        audit.setUpdatedAt(Instant.now());

        InterestRateAudit savedAudit = interestRateAuditRepository.save(audit);

        log.info("Interest rate override completed for loan {}. Old: {}%, New: {}%", 
                loan.getId(), oldRate, newRate);

        return mapToInterestRateAuditResponse(savedAudit);
    }

    /**
     * Get interest rate change history for a specific loan
     *
     * @param loanId The loan ID
     * @return List of audit records
     */
    @Transactional(readOnly = true)
    public Page<InterestRateAuditResponse> getLoanInterestRateHistory(UUID loanId, Pageable pageable) {
        log.debug("Fetching interest rate history for loan: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));

        return interestRateAuditRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToInterestRateAuditResponse);
    }

    /**
     * Get all interest rate audit records (paginated)
     *
     * @param pageable Pagination parameters
     * @return Page of audit records
     */
    @Transactional(readOnly = true)
    public Page<InterestRateAuditResponse> getAllInterestRateAudits(Pageable pageable) {
        log.debug("Fetching all interest rate audit records");

        return interestRateAuditRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToInterestRateAuditResponse);
    }

    // ==================== Mapper Methods ====================

    private SystemInterestRateResponse mapToSystemInterestRateResponse(SystemInterestSettings settings) {
        return SystemInterestRateResponse.builder()
                .id(settings.getId())
                .baseInterestRate(settings.getBaseInterestRate())
                .effectiveFrom(settings.getEffectiveFrom())
                .setByUsername(settings.getSetByUser() != null ? settings.getSetByUser().getUsername() : null)
                .setByUserId(settings.getSetByUser() != null ? settings.getSetByUser().getId() : null)
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    private InterestRateAuditResponse mapToInterestRateAuditResponse(InterestRateAudit audit) {
        return InterestRateAuditResponse.builder()
                .id(audit.getId())
                .loanId(audit.getLoan() != null ? audit.getLoan().getId() : null)
                .oldInterestRate(audit.getOldInterestRate())
                .newInterestRate(audit.getNewInterestRate())
                .changedByUsername(audit.getChangedBy() != null ? audit.getChangedBy().getUsername() : null)
                .changedByUserId(audit.getChangedBy() != null ? audit.getChangedBy().getId() : null)
                .changeReason(audit.getChangeReason())
                .createdAt(audit.getCreatedAt())
                .updatedAt(audit.getUpdatedAt())
                .build();
    }
}
