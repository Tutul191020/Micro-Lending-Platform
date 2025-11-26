package tutul.ex.QarzMukti.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_authorities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAuthority {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "authority_id", nullable = false)
    private Integer authorityId;
    
    @Column(name = "assigned_by")
    private UUID assignedBy;
    
    @Column(name = "assigned_at")
    private OffsetDateTime assignedAt;
    
    @PrePersist
    protected void onCreate() {
        assignedAt = OffsetDateTime.now();
    }
}