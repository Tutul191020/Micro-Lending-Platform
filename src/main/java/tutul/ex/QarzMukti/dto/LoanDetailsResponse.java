package tutul.ex.QarzMukti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for loan details with repayment schedule
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDetailsResponse {

    private LoanResponse loan;
    private List<LoanRepaymentResponse> repaymentSchedule;
    private BigDecimal totalRepayable;
    private BigDecimal totalInterest;
    private Integer remainingInstallments;
}
