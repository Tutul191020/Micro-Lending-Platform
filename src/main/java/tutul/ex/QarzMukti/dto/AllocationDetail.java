package tutul.ex.QarzMukti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationDetail {

    private UUID repaymentId;
    private LocalDate dueDate;
    private BigDecimal allocatedAmount;
    private String allocationType;
    private BigDecimal newRemainingAmount;
}
