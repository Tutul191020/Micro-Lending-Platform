package tutul.ex.QarzMukti.shared.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications",
       indexes = @Index(columnList = "user_id, is_read, created_at"))
@Data
public class Notification {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    private String type;

    private String entityType;

    private UUID entityId;

    private Boolean isRead = false;

    @Column(nullable = false)
    private Instant createdAt;
}
