// ── LeaderboardSnapshotRepository.java ──────────────────────────────────────
package vn.hcmute.edu.materialsservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.models.LeaderboardSnapshot;
import java.util.Optional;

@Repository
public interface LeaderboardSnapshotRepository extends MongoRepository<LeaderboardSnapshot, String> {
    Optional<LeaderboardSnapshot> findByTypeAndPeriod(String type, String period);
}

