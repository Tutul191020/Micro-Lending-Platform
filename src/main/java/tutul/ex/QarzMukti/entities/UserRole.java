package tutul.ex.QarzMukti.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_roles")
@Getter @Setter  @AllArgsConstructor @Builder
@NoArgsConstructor
public class UserRole {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;                    // ← Changed to Long

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;

    private Long assignedBy;
    private OffsetDateTime assignedAt;

    @PrePersist
    void onCreate() {
        this.assignedAt = OffsetDateTime.now();
    }
}