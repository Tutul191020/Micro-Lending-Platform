package tutul.ex.QarzMukti.enums;

/**
 * Enum representing the lifecycle status of a loan
 */
public enum LoanStatus {
    PENDING_APPROVAL("Loan application submitted, awaiting approval"),
    APPROVED("Loan approved, pending disbursement"),
    DISBURSED("Loan amount disbursed to user"),
    ACTIVE("Loan is active with ongoing repayments"),
    PARTIALLY_PAID("Some installments paid"),
    FULLY_PAID("All installments paid, loan closed"),
    OVERDUE("Payment overdue"),
    DEFAULTED("Loan defaulted"),
    REJECTED("Loan application rejected"),
    CANCELLED("Loan cancelled by user or system");

    private final String description;

    LoanStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
