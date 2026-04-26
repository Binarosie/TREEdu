package vn.hcmute.edu.materialsservice.repository;

import vn.hcmute.edu.materialsservice.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> { // ← String
    Optional<User> findByEmail(String email);

    // check email exists
    boolean existsByEmail(String email);

    // check user active
    boolean existsByIdAndIsActive(String userId, boolean isActive);

    Optional<User> findById(String userId);

    // 🔥 XÓA TẤT CẢ USER INACTIVE CÙNG EMAIL (auto-clean duplicates)
    void deleteByEmailAndIsActive(String email, boolean isActive);

    // 🔥 XÓA TẤT CẢ USER KHÁC CÙNG EMAIL TRỪ USER HIỆN TẠI
    void deleteByEmailAndIdNot(String email, String id);
}