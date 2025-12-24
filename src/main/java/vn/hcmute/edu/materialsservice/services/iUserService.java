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

    Optional<User> getUserById(UUID id);

    UserInfoDTO getUserInfoById(UUID id);

    UserDetailDTO getUserDetailById(UUID id);

    Page<User> getAllUsers(Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsByUserIdAndIsActive(UUID userId, boolean isActive);

    User updateMyProfile(UUID id, UpdateProfileRequest request);

    User updateUserByID(UUID id, UpdateUserRequest request);

    boolean changePasswordById(UUID id, String newPassword);

    void deactivateUser(UUID id);

    void activateUser(UUID id);

    int getTotalUsers();

    int getTotalMembers();

    int getInactiveMembers();

    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID id);
}