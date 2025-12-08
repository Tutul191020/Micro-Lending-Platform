package tutul.ex.QarzMukti.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tutul.ex.QarzMukti.shared.entities.LoanRepayment;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAllocationResult {

    private UUID transactionId;
    private BigDecimal totalAmount;
    private LoanRepayment primaryRepayment;
    private BigDecimal primaryAllocation;
    private List<AllocationDetail> allocations;

    /**
     * Allocation strategy used:
     * - EXACT: Payment equals remaining amount
     * - PARTIAL: Payment less than remaining amount (underpayment)
     * - OVERPAYMENT: Payment exceeds remaining amount
     */
    private String allocationStrategy;

    private List<LoanRepayment> affectedRepayments;
}
