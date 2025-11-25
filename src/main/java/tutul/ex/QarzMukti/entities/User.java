package tutul.ex.QarzMukti.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    private String fullName;

    private String email;

    private String phone;

    private Boolean enabled = true;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;
}
