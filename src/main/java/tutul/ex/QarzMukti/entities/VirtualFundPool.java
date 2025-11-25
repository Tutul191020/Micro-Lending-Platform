package tutul.ex.QarzMukti.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "virtual_fund_pools")
@Data
public class VirtualFundPool {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private BigDecimal balance;

    private String currency = "BDT";

    @Column(nullable = false)
    private Long version;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;
}
