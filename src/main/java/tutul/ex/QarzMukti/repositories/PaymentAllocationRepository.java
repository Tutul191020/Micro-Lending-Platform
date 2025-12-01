package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tutul.ex.QarzMukti.entities.LoanRepayment;
import tutul.ex.QarzMukti.entities.PaymentAllocation;
import tutul.ex.QarzMukti.entities.PaymentTransaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, UUID> {

    /**
     * Find all allocations for a specific payment transaction
     */
    List<PaymentAllocation> findByTransaction(PaymentTransaction transaction);

    /**
     * Find all allocations for a specific repayment installment
     */
    List<PaymentAllocation> findByRepayment(LoanRepayment repayment);

    /**
     * Calculate total allocated amount for a specific repayment
     */
    @Query("SELECT COALESCE(SUM(pa.allocatedAmount), 0) FROM PaymentAllocation pa " +
            "WHERE pa.repayment.id = :repaymentId")
    BigDecimal sumAllocatedAmountByRepayment(@Param("repaymentId") UUID repaymentId);

    /**
     * Find allocations by type for a transaction
     */
    List<PaymentAllocation> findByTransactionAndAllocationType(
            PaymentTransaction transaction,
            String allocationType);
}
