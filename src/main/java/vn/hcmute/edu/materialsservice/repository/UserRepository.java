package vn.hcmute.edu.materialsservice.repository;

import vn.hcmute.edu.materialsservice.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.services.IStreakService;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> { // ← String
    Optional<User> findByEmail(String email);


    boolean existsByEmail(String email);

    boolean existsByIdAndIsActive(String userId, boolean isActive);

    Optional<User> findById(String userId);

    void deleteByEmailAndIsActive(String email, boolean isActive);

    void deleteByEmailAndIdNot(String email, String id);


}
