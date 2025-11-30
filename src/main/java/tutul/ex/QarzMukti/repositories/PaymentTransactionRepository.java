package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.entities.PaymentTransaction;

import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    
    Optional<PaymentTransaction> findByIdempotencyKey(UUID idempotencyKey);
    
    Optional<PaymentTransaction> findByGatewayTxId(String gatewayTxId);
}