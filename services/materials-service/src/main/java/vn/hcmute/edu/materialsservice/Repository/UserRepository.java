package vn.hcmute.edu.materialsservice.Repository;

import vn.hcmute.edu.materialsservice.Model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends MongoRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    // check email exists
    boolean existsByEmail(String email);

    // check user active
    boolean existsByUserIdAndIsActive(UUID userId, boolean isActive);

    Optional<User> findByUserId(UUID userId);

    // 🔥 XÓA TẤT CẢ USER INACTIVE CÙNG EMAIL (auto-clean duplicates)
    void deleteByEmailAndIsActive(String email, boolean isActive);
}