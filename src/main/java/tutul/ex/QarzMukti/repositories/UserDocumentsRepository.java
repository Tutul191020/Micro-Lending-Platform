package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tutul.ex.QarzMukti.entities.UserDocuments;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDocumentsRepository extends JpaRepository<UserDocuments, UUID> {
    
    Optional<UserDocuments> findByUserId(UUID userId);
    
    boolean existsByUserId(UUID userId);
    
    void deleteByUserId(UUID userId);
}