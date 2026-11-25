package tutul.ex.QarzMukti.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundPoolResponse {
    private UUID poolId;
    private Long userId;
    private String username;
    private BigDecimal balance;
    private String currency;
    
    @Builder.Default
    private BigDecimal totalDisbursed = BigDecimal.ZERO;
    
    @Builder.Default
    private Integer activeLoans = 0;
    
    @Builder.Default
    private Integer overdueLoans = 0;
}
