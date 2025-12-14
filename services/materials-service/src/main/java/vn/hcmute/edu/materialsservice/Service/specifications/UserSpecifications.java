package vn.hcmute.edu.materialsservice.Service.specifications;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.materialsservice.Model.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSpecifications {

    private final MongoTemplate mongoTemplate;

    /**
     * Mongo equivalent of UserSpecifications.withFilters(...)
     */
    public Page<User> searchUsers(
            String search,
            String role,
            String status,
            Pageable pageable
    ) {

        Query query = new Query();

        // === 1. Fulltext Search: Name OR Email ===
        if (search != null && !search.isBlank()) {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("fullName").regex(search, "i"),
                    Criteria.where("email").regex(search, "i")
            ));
        }

        // === 2. Role filter (inheritance replacement)
        if (role != null && !role.isBlank()) {
            query.addCriteria(Criteria.where("_class")
                    .regex(roleToMongoClass(role), "i"));
        }

        // === 3. Status filter
        if (status != null && !status.isBlank()) {
            if (status.equalsIgnoreCase("active")) {
                query.addCriteria(Criteria.where("isActive").is(true));
            } else if (status.equalsIgnoreCase("inactive")) {
                query.addCriteria(Criteria.where("isActive").is(false));
            }
        }

        long total = mongoTemplate.count(query, User.class);
        query.with(pageable);

        List<User> users = mongoTemplate.find(query, User.class);

        return new PageImpl<>(users, pageable, total);
    }

    private String roleToMongoClass(String role) {
        return switch (role.toLowerCase()) {
            case "admin" -> "Admin";
            case "supporter" -> "Supporter";
            case "member" -> "Member";
            default -> "User";
        };
    }
}
