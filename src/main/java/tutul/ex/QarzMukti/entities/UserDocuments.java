package tutul.ex.QarzMukti.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDocuments {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;
    
    @Column(name = "national_id_document_path")
    private String nationalIdDocumentPath;
    
    @Column(name = "national_id_uploaded_at")
    private OffsetDateTime nationalIdUploadedAt;
    
    @Column(name = "electricity_bill_document_path")
    private String electricityBillDocumentPath;
    
    @Column(name = "electricity_bill_uploaded_at")
    private OffsetDateTime electricityBillUploadedAt;
    
    @Column(name = "profile_photo_path")
    private String profilePhotoPath;
    
    @Column(name = "profile_photo_uploaded_at")
    private OffsetDateTime profilePhotoUploadedAt;
    
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}