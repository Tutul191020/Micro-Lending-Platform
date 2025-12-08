package tutul.ex.QarzMukti.features.reconciliation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tutul.ex.QarzMukti.shared.dto.TransactionLogResponse;
import tutul.ex.QarzMukti.shared.entities.TransactionLog;
import tutul.ex.QarzMukti.shared.entities.User;
import tutul.ex.QarzMukti.shared.repositories.TransactionLogRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Service for logging all financial transactions.
 * Provides comprehensive audit trail for transparency.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionLogService {

    private final TransactionLogRepository transactionLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Log a financial transaction.
     */
    @Transactional
    public void logTransaction(
            String transactionType,
            BigDecimal amount,
            User fromUser,
            User toUser,
            UUID fromPoolId,
            UUID toPoolId,
            UUID referenceId,
            String referenceType,
            String description,
            Map<String, Object> metadata,
            User performedBy) {

        log.info("Logging transaction: Type={}, Amount={}, From={}, To={}", 
                transactionType, amount, 
                fromUser != null ? fromUser.getUsername() : "SYSTEM",
                toUser != null ? toUser.getUsername() : "SYSTEM");

        TransactionLog transactionLog = TransactionLog.builder()
                .transactionType(transactionType)
                .amount(amount)
                .fromUser(fromUser)
                .toUser(toUser)
                .fromPoolId(fromPoolId)
                .toPoolId(toPoolId)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .description(description)
                .performedBy(performedBy)
                .build();

        // Serialize metadata to JSON
        try {
            if (metadata != null && !metadata.isEmpty()) {
                transactionLog.setMetadata(objectMapper.writeValueAsString(metadata));
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize metadata for transaction log", e);
        }

        TransactionLog saved = transactionLogRepository.save(transactionLog);
        log.info("Transaction logged successfully: ID={}", saved.getId());
    }

    /**
     * Get transaction history for a user.
     */
    public Page<TransactionLogResponse> getTransactionHistory(Long userId, Pageable pageable) {
        Page<TransactionLog> logs = transactionLogRepository
                .findByFromUserIdOrToUserIdOrderByCreatedAtDesc(userId, userId, pageable);
        return logs.map(this::mapToResponse);
    }

    /**
     * Get transactions by type.
     */
    public Page<TransactionLogResponse> getTransactionsByType(String transactionType, Pageable pageable) {
        Page<TransactionLog> logs = transactionLogRepository
                .findByTransactionTypeOrderByCreatedAtDesc(transactionType, pageable);
        return logs.map(this::mapToResponse);
    }

    /**
     * Get all transactions.
     */
    public Page<TransactionLogResponse> getAllTransactions(Pageable pageable) {
        Page<TransactionLog> logs = transactionLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        return logs.map(this::mapToResponse);
    }

    private TransactionLogResponse mapToResponse(TransactionLog log) {
        return TransactionLogResponse.builder()
                .id(log.getId())
                .transactionType(log.getTransactionType())
                .amount(log.getAmount())
                .fromUserId(log.getFromUser() != null ? log.getFromUser().getId() : null)
                .fromUsername(log.getFromUser() != null ? log.getFromUser().getUsername() : null)
                .toUserId(log.getToUser() != null ? log.getToUser().getId() : null)
                .toUsername(log.getToUser() != null ? log.getToUser().getUsername() : null)
                .referenceId(log.getReferenceId())
                .referenceType(log.getReferenceType())
                .description(log.getDescription())
                .performedById(log.getPerformedBy().getId())
                .performedByUsername(log.getPerformedBy().getUsername())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
