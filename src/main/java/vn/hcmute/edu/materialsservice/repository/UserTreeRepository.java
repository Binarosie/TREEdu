// ── UserTreeRepository.java ──────────────────────────────────────────────────
package vn.hcmute.edu.materialsservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.models.UserTree;
import java.util.Optional;

@Repository
public interface UserTreeRepository extends MongoRepository<UserTree, String> {
    Optional<UserTree> findByUserId(String userId);
}

