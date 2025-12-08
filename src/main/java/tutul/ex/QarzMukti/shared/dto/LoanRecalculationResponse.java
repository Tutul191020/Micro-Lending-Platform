package tutul.ex.QarzMukti.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for loan recalculation showing old vs new schedule
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRecalculationResponse {

    private LoanResponse loan;
    private BigDecimal oldInterestRate;
    private BigDecimal newInterestRate;
    private int paidInstallments;
    private int remainingInstallments;
    private BigDecimal remainingPrincipal;
    private BigDecimal oldTotalInterest;
    private BigDecimal newTotalInterest;
    private BigDecimal interestSavings;
    private List<LoanRepaymentResponse> newRepaymentSchedule;
}
