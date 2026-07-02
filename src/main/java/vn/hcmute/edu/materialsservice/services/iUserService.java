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
    User createMember(CreateUserRequest request);

    User createManager(CreateUserRequest request);

    Optional<User> getUserById(String id);

    UserInfoDTO getUserInfoById(String id);

    UserDetailDTO getUserDetailById(String id);

    Page<User> getAllUsers(Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsByUserIdAndIsActive(String userId, boolean isActive);

    User updateMyProfile(String id, UpdateProfileRequest request);

    User updateUserByID(String id, UpdateUserRequest request);

    boolean changePasswordById(String id, String newPassword);

    void deactivateUser(String id);

    void activateUser(String id);

    int getTotalUsers();

    int getTotalMembers();

    int getInactiveMembers();

    Optional<User> findByEmail(String email);

    Optional<User> findById(String id);

    boolean addXpToMember(String userId, int xpToAdd);

    int calculateLevel(int totalXp);
}
