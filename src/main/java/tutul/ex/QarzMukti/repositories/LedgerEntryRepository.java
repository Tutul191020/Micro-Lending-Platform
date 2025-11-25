package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.entities.LedgerEntry;

import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {}