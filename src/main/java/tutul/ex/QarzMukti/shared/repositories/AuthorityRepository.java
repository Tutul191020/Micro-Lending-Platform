package tutul.ex.QarzMukti.shared.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tutul.ex.QarzMukti.shared.entities.Authority;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Integer> {
    
    Optional<Authority> findByName(String name);
    
    boolean existsByName(String name);
    
    List<Authority> findByResource(String resource);
    
    List<Authority> findByAction(String action);
    
    List<Authority> findByResourceAndAction(String resource, String action);
    
    @Query("SELECT a FROM Authority a ORDER BY a.resource, a.action")
    List<Authority> findAllOrderedByResourceAndAction();
}