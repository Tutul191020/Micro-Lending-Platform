package tutul.ex.QarzMukti.shared.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.shared.entities.FundRequest;

import java.util.List;
import java.util.UUID;

public interface FundRequestRepository extends JpaRepository<FundRequest, UUID> {
    List<FundRequest> findByStatus(String status);
    Page<FundRequest> findByRequestedById(Long userId, Pageable pageable);
    Page<FundRequest> findByStatus(String status, Pageable pageable);
}
