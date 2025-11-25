package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.entities.UserRole;

import java.util.UUID;


public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {}