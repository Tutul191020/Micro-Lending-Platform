package tutul.ex.QarzMukti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundRequestReviewRequest {
    private Boolean approved;
    private String comments;
}
