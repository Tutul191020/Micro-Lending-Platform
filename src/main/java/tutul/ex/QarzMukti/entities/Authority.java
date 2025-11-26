package tutul.ex.QarzMukti.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "authorities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Authority {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name; // e.g., USER:CREATE, LOAN:APPROVE
    
    @Column(name = "description", nullable = false)
    private String description;
    
    @Column(name = "resource", nullable = false, length = 50)
    private String resource; // e.g., USER, LOAN, FUND
    
    @Column(name = "action", nullable = false, length = 50)
    private String action; // e.g., CREATE, READ, UPDATE, DELETE, APPROVE
    
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}