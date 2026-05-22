// ── TreeEventRepository.java ─────────────────────────────────────────────────
package vn.hcmute.edu.materialsservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.models.TreeEvent;
import java.util.List;

@Repository
public interface TreeEventRepository extends MongoRepository<TreeEvent, String> {
    List<TreeEvent> findByUserIdOrderByCreatedAtDesc(String userId);
}

