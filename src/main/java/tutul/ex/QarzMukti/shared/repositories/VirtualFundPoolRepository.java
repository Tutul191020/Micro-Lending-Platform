package tutul.ex.QarzMukti.shared.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.shared.entities.VirtualFundPool;

import java.util.Optional;
import java.util.UUID;

public interface VirtualFundPoolRepository extends JpaRepository<VirtualFundPool, UUID> {
    Optional<VirtualFundPool> findByUserId(Long userId);
}