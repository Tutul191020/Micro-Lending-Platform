package tutul.ex.QarzMukti.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tutul.ex.QarzMukti.common.ValidationException;
import tutul.ex.QarzMukti.dto.FundAllocationRequest;
import tutul.ex.QarzMukti.dto.FundPoolResponse;
import tutul.ex.QarzMukti.dto.TreasuryMintRequest;
import tutul.ex.QarzMukti.entities.User;
import tutul.ex.QarzMukti.entities.VirtualFundPool;
import tutul.ex.QarzMukti.repositories.UserRepository;
import tutul.ex.QarzMukti.repositories.VirtualFundPoolRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TreasuryService {

    private final VirtualFundPoolRepository virtualFundPoolRepository;
    private final UserRepository userRepository;
    private final LedgerService ledgerService;

    /**
     * Mint new funds into the system treasury.
     * Only Super User can perform this operation.
     */
    @Transactional
    public FundPoolResponse mintFund(TreasuryMintRequest request, User adminUser) {
        log.info("Minting funds: Amount={}, Admin={}", request.getAmount(), adminUser.getUsername());

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Mint amount must be positive");
        }

        // Get or create treasury pool for the admin user
        VirtualFundPool treasuryPool = getOrCreatePool(adminUser);

        // Update balance
        treasuryPool.setBalance(treasuryPool.getBalance().add(request.getAmount()));
        treasuryPool.setUpdatedAt(Instant.now());
        treasuryPool = virtualFundPoolRepository.save(treasuryPool);

        // Record in ledger
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("description", "Minted new funds into treasury");
        metadata.put("mintedBy", adminUser.getUsername());
        
        ledgerService.recordTransaction(
                null, // No debit pool for minting (money created from thin air)
                treasuryPool,
                request.getAmount(),
                "TREASURY_DEPOSIT",
                null, 
                "SYSTEM",
                metadata,
                adminUser
        );

        log.info("Funds minted successfully. New balance: {}", treasuryPool.getBalance());
        return mapToFundPoolResponse(treasuryPool);
    }

    /**
     * Allocate funds from Treasury to an Accountant.
     */
    @Transactional
    public FundPoolResponse allocateFundToAccountant(FundAllocationRequest request, User adminUser) {
        log.info("Allocating funds: AccountantId={}, Amount={}", request.getAccountantId(), request.getAmount());

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Allocation amount must be positive");
        }

        // Validate source (Treasury)
        VirtualFundPool treasuryPool = getOrCreatePool(adminUser);
        if (treasuryPool.getBalance().compareTo(request.getAmount()) < 0) {
            throw new ValidationException("Insufficient treasury funds. Available: " + treasuryPool.getBalance());
        }

        // Validate destination (Accountant)
        User accountant = userRepository.findById(request.getAccountantId())
                .orElseThrow(() -> new ValidationException("Accountant not found with ID: " + request.getAccountantId()));
        
        VirtualFundPool accountantPool = getOrCreatePool(accountant);

        // Perform transfer
        treasuryPool.setBalance(treasuryPool.getBalance().subtract(request.getAmount()));
        accountantPool.setBalance(accountantPool.getBalance().add(request.getAmount()));
        
        treasuryPool.setUpdatedAt(Instant.now());
        accountantPool.setUpdatedAt(Instant.now());

        virtualFundPoolRepository.save(treasuryPool);
        virtualFundPoolRepository.save(accountantPool);

        // Record in ledger
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("description", "Fund allocation to accountant");
        metadata.put("allocatedBy", adminUser.getUsername());
        metadata.put("accountantUsername", accountant.getUsername());

        ledgerService.recordTransaction(
                treasuryPool,
                accountantPool,
                request.getAmount(),
                "FUND_ALLOCATION",
                null,
                "SYSTEM",
                metadata,
                adminUser
        );

        log.info("Funds allocated successfully to accountant: {}", accountant.getUsername());
        return mapToFundPoolResponse(accountantPool);
    }

    /**
     * Get or create a virtual fund pool for a user.
     */
    @Transactional
    public VirtualFundPool getOrCreatePool(User user) {
        return virtualFundPoolRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    log.info("Creating new fund pool for user: {}", user.getUsername());
                    VirtualFundPool pool = new VirtualFundPool();
                    pool.setId(UUID.randomUUID());
                    pool.setUser(user);
                    pool.setBalance(BigDecimal.ZERO);
                    pool.setCurrency("BDT");
                    pool.setVersion(0L);
                    pool.setCreatedAt(Instant.now());
                    return virtualFundPoolRepository.save(pool);
                });
    }
    /**
     * Get the balance of a user's fund pool.
     */
    @Transactional
    public FundPoolResponse getUserFundPool(User user) {
        VirtualFundPool pool = getOrCreatePool(user);
        return mapToFundPoolResponse(pool);
    }

    private FundPoolResponse mapToFundPoolResponse(VirtualFundPool pool) {
        return FundPoolResponse.builder()
                .poolId(pool.getId())
                .userId(pool.getUser().getId())
                .username(pool.getUser().getUsername())
                .balance(pool.getBalance())
                .currency(pool.getCurrency())
                .build();
    }
}
