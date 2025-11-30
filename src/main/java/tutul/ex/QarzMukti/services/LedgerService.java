package tutul.ex.QarzMukti.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tutul.ex.QarzMukti.entities.LedgerEntry;
import tutul.ex.QarzMukti.entities.User;
import tutul.ex.QarzMukti.entities.VirtualFundPool;
import tutul.ex.QarzMukti.repositories.LedgerEntryRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Service for recording all financial transactions in the ledger.
 * This is the source of truth for all money movements in the system.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final ObjectMapper objectMapper;

    /**
     * Records a financial transaction in the ledger.
     * This follows double-entry bookkeeping principles.
     *
     * @param debitPool     The pool where money is coming FROM (can be null for minting)
     * @param creditPool    The pool where money is going TO (can be null for burning)
     * @param amount        The amount to transfer
     * @param entryType     Type of transaction (e.g., "TREASURY_DEPOSIT", "LOAN_DISBURSEMENT")
     * @param referenceId   ID of the related entity (e.g., Loan ID, Payment ID)
     * @param referenceType Type of the related entity
     * @param metadata      Additional context as a map
     * @param performedBy   User performing the action
     */
    @Transactional
    public void recordTransaction(
            VirtualFundPool debitPool,
            VirtualFundPool creditPool,
            BigDecimal amount,
            String entryType,
            UUID referenceId,
            String referenceType,
            Map<String, Object> metadata,
            User performedBy) {

        log.info("Recording ledger transaction: Type={}, Amount={}, Ref={}", entryType, amount, referenceId);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }

        LedgerEntry entry = new LedgerEntry();
        entry.setId(UUID.randomUUID());
        entry.setEntryType(entryType);
        entry.setReferenceId(referenceId);
        entry.setReferenceType(referenceType);
        entry.setDebitPool(debitPool);
        entry.setCreditPool(creditPool);
        entry.setAmount(amount);
        entry.setCurrency("BDT");
        entry.setCreatedBy(performedBy);
        entry.setCreatedAt(Instant.now());

        // Serialize metadata to JSON
        try {
            if (metadata != null && !metadata.isEmpty()) {
                entry.setMetadata(objectMapper.writeValueAsString(metadata));
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize metadata for ledger entry", e);
        }

        LedgerEntry saved = ledgerEntryRepository.save(entry);
        log.info("Ledger entry created: ID={}", saved.getId());
    }

    /**
     * Helper method to create a loan disbursement ledger entry
     */
    @Transactional
    public void createLoanDisbursementEntry(
            VirtualFundPool debitPool,
            VirtualFundPool creditPool,
            BigDecimal amount,
            UUID loanId,
            User performedBy) {
        
        recordTransaction(
                debitPool,
                creditPool,
                amount,
                "LOAN_DISBURSEMENT",
                loanId,
                "LOAN",
                Map.of("description", "Loan disbursed"),
                performedBy
        );
    }
    /**
     * Helper method to create a loan repayment ledger entry
     */
    @Transactional
    public void createLoanRepaymentEntry(
            VirtualFundPool debitPool,
            VirtualFundPool creditPool,
            BigDecimal amount,
            UUID repaymentId,
            User performedBy) {
        
        recordTransaction(
                debitPool,
                creditPool,
                amount,
                "LOAN_REPAYMENT",
                repaymentId,
                "LOAN_REPAYMENT",
                Map.of("description", "Loan repayment received"),
                performedBy
        );
    }
}
