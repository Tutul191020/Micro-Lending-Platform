package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.entities.User;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {}