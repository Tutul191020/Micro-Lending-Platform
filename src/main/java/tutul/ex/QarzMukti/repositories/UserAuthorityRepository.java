package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tutul.ex.QarzMukti.entities.Authority;
import tutul.ex.QarzMukti.entities.User;
import tutul.ex.QarzMukti.entities.UserAuthority;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserAuthorityRepository extends JpaRepository<UserAuthority, UUID> {

    @Query("SELECT a FROM Authority a JOIN UserAuthority ua ON a.id = ua.authorityId WHERE ua.userId = :userId")
    Set<Authority> findAuthoritiesByUserId(@Param("userId") UUID userId);

    @Query("SELECT u FROM User u JOIN UserAuthority ua ON u.id = ua.userId WHERE ua.authorityId = :authorityId")
    List<User> findUsersByAuthorityId(@Param("authorityId") Integer authorityId);

    boolean existsByUserIdAndAuthorityId(UUID userId, Integer authorityId);

    void deleteByUserId(UUID userId);

    void deleteByUserIdAndAuthorityId(UUID userId, Integer authorityId);

    long countByAuthorityId(Integer authorityId);

    long countByUserId(UUID userId);

    @Query("SELECT DISTINCT a FROM Authority a " +
            "WHERE a.id IN (SELECT ua.authorityId FROM UserAuthority ua WHERE ua.userId = :userId) " +
            "OR a.id IN (SELECT ra.authorityId FROM RoleAuthority ra " +
            "            JOIN UserRole ur ON ra.roleId = ur.roleId " +
            "            WHERE ur.userId = :userId)")
    Set<Authority> findAllUserAuthorities(@Param("userId") UUID userId);
}
