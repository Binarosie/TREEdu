package vn.hcmute.edu.materialsservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.hcmute.edu.materialsservice.models.UserDailyMission;
import java.time.LocalDate;
import java.util.Optional;

public interface UserDailyMissionRepository extends MongoRepository<UserDailyMission, String> {
    Optional<UserDailyMission> findByUserIdAndDate(String userId, LocalDate date);
}
