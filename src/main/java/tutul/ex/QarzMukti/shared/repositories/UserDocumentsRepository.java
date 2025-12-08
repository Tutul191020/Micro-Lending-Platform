package tutul.ex.QarzMukti.shared.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tutul.ex.QarzMukti.shared.entities.UserDocuments;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDocumentsRepository extends JpaRepository<UserDocuments, UUID> {

    Optional<UserDocuments> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    void deleteByUserId(Long userId);

}