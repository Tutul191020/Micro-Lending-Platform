package tutul.ex.QarzMukti.shared.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tutul.ex.QarzMukti.shared.entities.SystemInterestSettings;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SystemInterestSettingsRepository extends JpaRepository<SystemInterestSettings, UUID> {
    
    /**
     * Find the most recent active interest rate setting
     */
    @Query("SELECT s FROM SystemInterestSettings s WHERE s.effectiveFrom <= :now ORDER BY s.effectiveFrom DESC LIMIT 1")
    Optional<SystemInterestSettings> findLatestEffectiveRate(Instant now);
    
    /**
     * Get all interest rate settings ordered by effective date (newest first)
     */
    Page<SystemInterestSettings> findAllByOrderByEffectiveFromDesc(Pageable pageable);
}