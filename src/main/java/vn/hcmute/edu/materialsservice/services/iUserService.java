package vn.hcmute.edu.materialsservice.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.hcmute.edu.materialsservice.dtos.UserDetailDTO;
import vn.hcmute.edu.materialsservice.dtos.UserInfoDTO;
import vn.hcmute.edu.materialsservice.dtos.request.users.CreateUserRequest;
import vn.hcmute.edu.materialsservice.dtos.request.users.UpdateProfileRequest;
import vn.hcmute.edu.materialsservice.dtos.request.users.UpdateUserRequest;
import vn.hcmute.edu.materialsservice.models.User;

import java.util.Optional;
import java.util.UUID;

public interface iUserService {
    // Tạo user dựa trên loại được chỉ định trong request
    User createMember(CreateUserRequest request);

    User createManager(CreateUserRequest request);

    Optional<User> getUserById(String id); // ← UUID → String

    UserInfoDTO getUserInfoById(String id); // ← UUID → String

    UserDetailDTO getUserDetailById(String id); // ← UUID → String

    Page<User> getAllUsers(Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsByUserIdAndIsActive(String userId, boolean isActive); // ← UUID → String

    User updateMyProfile(String id, UpdateProfileRequest request); // ← UUID → String

    User updateUserByID(String id, UpdateUserRequest request); // ← UUID → String

    boolean changePasswordById(String id, String newPassword); // ← UUID → String

    void deactivateUser(String id); // ← UUID → String

    void activateUser(String id); // ← UUID → String

    int getTotalUsers();

    int getTotalMembers();

    int getInactiveMembers();

    Optional<User> findByEmail(String email);

    Optional<User> findById(String id); // ← UUID → String

    boolean addXpToMember(String userId, int xpToAdd);

    int calculateLevel(int totalXp);
}
