package tutul.ex.QarzMukti.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.entities.TransactionLog;

import java.util.UUID;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, UUID> {
    Page<TransactionLog> findByFromUserIdOrToUserIdOrderByCreatedAtDesc(Long fromUserId, Long toUserId, Pageable pageable);
    Page<TransactionLog> findByTransactionTypeOrderByCreatedAtDesc(String transactionType, Pageable pageable);
    Page<TransactionLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
