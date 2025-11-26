package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tutul.ex.QarzMukti.entities.Role;
import tutul.ex.QarzMukti.entities.User;
import tutul.ex.QarzMukti.entities.UserRole;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    
    @Query("SELECT r FROM Role r JOIN UserRole ur ON r.id = ur.roleId WHERE ur.userId = :userId")
    Set<Role> findRolesByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT u FROM User u JOIN UserRole ur ON u.id = ur.userId WHERE ur.roleId = :roleId")
    List<User> findUsersByRoleId(@Param("roleId") Integer roleId);
    
    boolean existsByUserIdAndRoleId(UUID userId, Integer roleId);
    
    void deleteByUserId(UUID userId);
    
    void deleteByUserIdAndRoleId(UUID userId, Integer roleId);
    
    long countByRoleId(Integer roleId);
    
    long countByUserId(UUID userId);
    
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.userId = :userId")
    long countUserRoles(@Param("userId") UUID userId);
}