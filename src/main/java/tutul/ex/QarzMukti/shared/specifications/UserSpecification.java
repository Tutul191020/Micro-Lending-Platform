package tutul.ex.QarzMukti.shared.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import tutul.ex.QarzMukti.shared.dto.UserSearchCriteria;
import tutul.ex.QarzMukti.shared.entities.User;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> withFilters(UserSearchCriteria c) {
        return (root, query, cb) -> {

            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            // =========== Basic User Filters ===========
            if (c.getUsername() != null) {
                predicates.add(cb.like(cb.lower(root.get("username")), "%" + c.getUsername().toLowerCase() + "%"));
            }

            if (c.getEmail() != null) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + c.getEmail().toLowerCase() + "%"));
            }

            if (c.getFullName() != null) {
                predicates.add(cb.like(cb.lower(root.get("fullName")), "%" + c.getFullName().toLowerCase() + "%"));
            }

            if (c.getNationalId() != null) {
                predicates.add(cb.equal(root.get("nationalId"), c.getNationalId()));
            }

            if (c.getEnabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), c.getEnabled()));
            }

            if (c.getVerified() != null) {
                predicates.add(cb.equal(root.get("verified"), c.getVerified()));
            }

            if (c.getVerificationStatus() != null) {
                predicates.add(cb.equal(root.get("verificationStatus"), c.getVerificationStatus()));
            }

            if (c.getRoleNames() != null && !c.getRoleNames().isEmpty()) {

                // user_roles table join
                Join<Object, Object> urJoin = root.join("userRoles", JoinType.LEFT);

                // roles table join
                Join<Object, Object> roleJoin = urJoin.join("role", JoinType.LEFT);

                predicates.add(roleJoin.get("name").in(c.getRoleNames()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

