package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.entities.SystemInterestSettings;

import java.util.UUID;

public interface SystemInterestSettingsRepository extends JpaRepository<SystemInterestSettings, UUID> {}