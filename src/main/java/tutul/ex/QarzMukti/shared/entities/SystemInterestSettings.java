package tutul.ex.QarzMukti.shared.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "system_interest_settings")
@Data
public class SystemInterestSettings {

    @Id
    private UUID id;

    @Column(nullable = false)
    private BigDecimal baseInterestRate;

    @Column(nullable = false)
    private Instant effectiveFrom;

    @ManyToOne
    @JoinColumn(name = "set_by_user_id")
    private User setByUser;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
