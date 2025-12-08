package tutul.ex.QarzMukti.features.loan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tutul.ex.QarzMukti.shared.entities.Loan;
import tutul.ex.QarzMukti.shared.entities.LoanRepayment;
import tutul.ex.QarzMukti.shared.repositories.LoanRepaymentRepository;
import tutul.ex.QarzMukti.shared.repositories.LoanRepository;
import tutul.ex.QarzMukti.features.reconciliation.LedgerService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanMonitoringService {

    private final LoanRepaymentRepository loanRepaymentRepository;
    private final LoanRepository loanRepository;
    private final LedgerService ledgerService; // To record late fee as revenue? Or just update loan.

    // Configurable late fee (could be from DB/Config)
    private static final BigDecimal DEFAULT_LATE_FEE = new BigDecimal("500.00");

    /**
     * Daily Cron Job to check for overdue installments
     * Runs every day at 00:01 AM
     */
    @Scheduled(cron = "0 1 0 * * ?")
    @Transactional
    public void checkOverdueInstallments() {
        log.info("Starting daily overdue check...");
        LocalDate today = LocalDate.now();

        // 1. Find all PENDING installments that are past due date
        List<LoanRepayment> overdueRepayments = loanRepaymentRepository.findByStatusAndDueDateBefore("PENDING", today);

        log.info("Found {} overdue installments.", overdueRepayments.size());

        for (LoanRepayment repayment : overdueRepayments) {
            processOverdueRepayment(repayment);
        }

        // 2. Check for Loans that need status update (DEFAULT_RISK)
        // We collect unique loans from the overdue repayments we just processed
        Set<Loan> affectedLoans = overdueRepayments.stream()
                .map(LoanRepayment::getLoan)
                .collect(Collectors.toSet());

        for (Loan loan : affectedLoans) {
            checkAndUpdateLoanRiskStatus(loan);
        }

        log.info("Daily overdue check completed.");
    }

    private void processOverdueRepayment(LoanRepayment repayment) {
        log.info("Marking repayment {} as OVERDUE", repayment.getId());

        repayment.setStatus("OVERDUE");

        // Apply Late Fee
        // We only apply if fee hasn't been applied yet (check if feeDue is zero)
        // Or we could apply it every month it's late? Usually it's one-time per
        // installment or recurring.
        // For simplicity, we apply one-time flat fee when it becomes overdue.
        if (repayment.getFeeDue().compareTo(BigDecimal.ZERO) == 0) {
            repayment.setFeeDue(DEFAULT_LATE_FEE);
            repayment.setTotalDue(repayment.getTotalDue().add(DEFAULT_LATE_FEE));

            log.info("Applied late fee of {} to repayment {}", DEFAULT_LATE_FEE, repayment.getId());

            // Notify User (Stub)
            notifyUser(repayment);
        }

        loanRepaymentRepository.save(repayment);
    }

    private void checkAndUpdateLoanRiskStatus(Loan loan) {
        long overdueCount = loanRepaymentRepository.countByLoanAndStatus(loan, "OVERDUE");

        if (overdueCount >= 2) {
            if (!"DEFAULT_RISK".equals(loan.getStatus()) && !"DEFAULTED".equals(loan.getStatus())) {
                log.warn("Loan {} is now DEFAULT_RISK ({} overdue installments)", loan.getId(), overdueCount);
                loan.setStatus("DEFAULT_RISK");
                loanRepository.save(loan);

                // Notify Admin/Accountant
                log.warn("NOTIFICATION: Loan {} is at risk!", loan.getId());
            }
        }

        // If 3 or more, maybe DEFAULTED? User said "If 2 month OVERDUE change to
        // DEFAULT_RISK".
        // We can add logic for DEFAULTED later if needed.
    }

    private void notifyUser(LoanRepayment repayment) {
        // Placeholder for NotificationService
        log.info("NOTIFICATION TO USER {}: Your installment of {} due on {} is OVERDUE. Late fee of {} applied.",
                repayment.getLoan().getUser().getUsername(),
                repayment.getTotalDue().subtract(repayment.getFeeDue()), // Original amount
                repayment.getDueDate(),
                repayment.getFeeDue());
    }
}
