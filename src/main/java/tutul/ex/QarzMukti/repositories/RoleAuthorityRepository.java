package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tutul.ex.QarzMukti.entities.Authority;
import tutul.ex.QarzMukti.entities.Role;
import tutul.ex.QarzMukti.entities.RoleAuthority;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RoleAuthorityRepository extends JpaRepository<RoleAuthority, UUID> {
    
    @Query("SELECT a FROM Authority a JOIN RoleAuthority ra ON a.id = ra.authorityId WHERE ra.roleId = :roleId")
    Set<Authority> findAuthoritiesByRoleId(@Param("roleId") Integer roleId);
    
    @Query("SELECT r FROM Role r JOIN RoleAuthority ra ON r.id = ra.roleId WHERE ra.authorityId = :authorityId")
    List<Role> findRolesByAuthorityId(@Param("authorityId") Integer authorityId);
    
    boolean existsByRoleIdAndAuthorityId(Integer roleId, Integer authorityId);
    
    void deleteByRoleId(Integer roleId);
    
    void deleteByRoleIdAndAuthorityId(Integer roleId, Integer authorityId);
    
    long countByAuthorityId(Integer authorityId);
    
    long countByRoleId(Integer roleId);
}