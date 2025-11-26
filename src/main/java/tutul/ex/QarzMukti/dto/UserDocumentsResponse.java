package tutul.ex.QarzMukti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDocumentsResponse {
    private String nationalIdDocumentPath;
    private String electricityBillDocumentPath;
    private String profilePhotoPath;
    private OffsetDateTime nationalIdUploadedAt;
    private OffsetDateTime electricityBillUploadedAt;
    private OffsetDateTime profilePhotoUploadedAt;
}