package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.entities.VirtualFundPool;

import java.util.Optional;
import java.util.UUID;

public interface VirtualFundPoolRepository extends JpaRepository<VirtualFundPool, UUID> {
    Optional<VirtualFundPool> findByUserId(Long userId);
}