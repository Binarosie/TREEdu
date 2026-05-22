package vn.hcmute.edu.materialsservice.services.impl;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.materialsservice.dtos.UserDetailDTO;
import vn.hcmute.edu.materialsservice.dtos.UserInfoDTO;
import vn.hcmute.edu.materialsservice.dtos.request.users.CreateUserRequest;
import vn.hcmute.edu.materialsservice.dtos.request.users.UpdateProfileRequest;
import vn.hcmute.edu.materialsservice.dtos.request.users.UpdateUserRequest;
import vn.hcmute.edu.materialsservice.dtos.response.BadRequestError;
import vn.hcmute.edu.materialsservice.dtos.response.ConflictError;
import vn.hcmute.edu.materialsservice.dtos.response.InternalServerError;
import vn.hcmute.edu.materialsservice.dtos.response.NotFoundError;
import vn.hcmute.edu.materialsservice.Enum.EUserRole;
import vn.hcmute.edu.materialsservice.models.Member;
import vn.hcmute.edu.materialsservice.models.User;
import vn.hcmute.edu.materialsservice.repository.UserRepository;
import vn.hcmute.edu.materialsservice.services.EmailService;
import vn.hcmute.edu.materialsservice.services.factories.iUserFactory;
import vn.hcmute.edu.materialsservice.services.iUserService;
import vn.hcmute.edu.materialsservice.services.strategies.AdminUpdateOtherUserStrategy;
import vn.hcmute.edu.materialsservice.services.strategies.iUserUpdateStrategy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements iUserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final List<iUserFactory> userFactories;
    private final List<iUserUpdateStrategy> updateStrategies;

    private final AdminUpdateOtherUserStrategy adminUpdateOtherUserStrategy;

    private iUserFactory getFactory(String userType) {
        return userFactories.stream()
                .filter(factory -> factory.supports(userType))
                .findFirst()
                .orElseThrow(() -> new BadRequestError("Invalid user type: " + userType));
    }

    private iUserUpdateStrategy getUpdateStrategy(User user) {
        return updateStrategies.stream()
                .filter(strategy -> strategy.supports(user))
                .findFirst()
                .orElseThrow(() -> new BadRequestError("Unsupported user type"));
    }

    @Override
    public User createMember(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictError("User already exists with email: " + request.getEmail());
        }

        iUserFactory factory = getFactory("MEMBER");
        Member user = (Member) factory.createUser(request);

        user.setId(UUID.randomUUID().toString());
        user.setCreatedOn(java.time.LocalDateTime.now());
        user.setModifiedOn(java.time.LocalDateTime.now());

        // ===== Map field mới =====
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getAvatarUrl()   != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getBirthYear()   != null) user.setBirthYear(request.getBirthYear());
        if (request.getAddress()     != null) user.setAddress(request.getAddress());
        if (request.getGender()      != null) user.setGender(request.getGender());

        try {
            int code = (int) ((Math.random() * 900000) + 100000);
            emailService.sendVerificationEmail(user.getEmail(), String.valueOf(code));
        } catch (MessagingException e) {
            throw new InternalServerError("Could not send verification email");
        }

        return userRepository.save(user);
    }
    public User createOAuthMember(String email, String fullName) {

        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    // Nếu user đã tồn tại, kích hoạt nếu chưa active
                    if (!existingUser.isActive()) {
                        existingUser.setActive(true);
                        return userRepository.save(existingUser);
                    }
                    return existingUser;
                })
                .orElseGet(() -> {
                    // Chỉ tạo mới nếu chưa tồn tại
                    CreateUserRequest request = new CreateUserRequest();
                    request.setEmail(email);
                    request.setFullName(fullName);
                    request.setPassword("12312345"); // dummy password
                    request.setUserType("MEMBER");

                    iUserFactory factory = getFactory("MEMBER");
                    Member member = (Member) factory.createUser(request);
                    member.setActive(true); // OAuth user đã được xác thực

                    return userRepository.save(member);
                });
    }

    // @Override
    // public User createManager(CreateUserRequest request) {
    // if (userRepository.existsByEmail(request.getEmail())) {
    // throw new ConflictError("User already exists with email: " +
    // request.getEmail());
    // }
    //
    // iUserFactory factory = getFactory("SUPPORTER");
    //// iUserFactory factory = getFactory(request.getUserType());
    // User user = factory.createUser(request);
    // return userRepository.save(user);
    // }

    @Override
    public User createManager(CreateUserRequest request) {
        log.info("📨 Received createManager request for: {}", request.getEmail());
        log.info("📝 Request details - userType: {}, fullName: {}", request.getUserType(), request.getFullName());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictError("User already exists with email: " + request.getEmail());
        }

        // FIX: Uncomment dòng này để dùng userType từ request
        iUserFactory factory = getFactory(request.getUserType());
        log.info("🏭 Selected factory: {}", factory.getClass().getSimpleName());

        User user = factory.createUser(request);
        log.info("💾 Saving user to database - Type: {}, Email: {}", user.getClass().getSimpleName(), user.getEmail());

        User savedUser = userRepository.save(user);
        log.info("✅ User saved successfully with ID: {}", savedUser.getId());

        return savedUser;
    }

    @Override
    public Optional<User> getUserById(String id) { // ← UUID → String
        return userRepository.findById(id);
    }

    @Override
    public UserInfoDTO getUserInfoById(String id) { // ← UUID → String
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundError("User not found with id: " + id));

        UserInfoDTO dto = new UserInfoDTO();
        dto = dto.mapToUserInfo(user);
        return dto;
    }

    @Override
    public UserDetailDTO getUserDetailById(String id) { // ← UUID → String
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundError("User not found with id: " + id));
        return UserDetailDTO.mapTo(user);
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUserIdAndIsActive(String userId, boolean isActive) { // ← UUID → String
        return userRepository.existsByIdAndIsActive(userId, isActive);
    }

    @Override
    public User updateMyProfile(String id, UpdateProfileRequest request) { // ← UUID → String
        Optional<User> optUser = userRepository.findById(id);
        if (!optUser.isPresent()) {
            throw new NotFoundError("User not found with id: " + id);
        }
        iUserUpdateStrategy updateStrategy = getUpdateStrategy(optUser.get());
        User user = optUser.get();
        updateStrategy.updateProfile(user, request);
        return userRepository.save(user);
    }

    @Override
    public User updateUserByID(String id, UpdateUserRequest request) { // ← UUID → String
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundError("User not found with id: " + id));

        getUpdateStrategy(user).update(user, request);
        return userRepository.save(user);
    }

    @Transactional // ← Thêm annotation này vào method
    public User adminUpdateUser(String targetUserId, UpdateUserRequest request, EUserRole currentUserRole) { // ← UUID →
                                                                                                             // String
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundError("User not found with id: " + targetUserId));

        EUserRole targetRole = EUserRole.fromUser(targetUser);
        log.info("Admin updating user: {} (current type: {})", targetUser.getEmail(), targetRole);

        // Sử dụng AdminUpdateOtherUserStrategy
        User updatedUser = adminUpdateOtherUserStrategy.updateByAdmin(targetUser, request, currentUserRole);

        EUserRole newRole = EUserRole.fromUser(updatedUser);
        log.info("User updated successfully. New type: {}", newRole);

        return updatedUser;
    }

    @Override
    public boolean changePasswordById(String id, String newPassword) { // ← UUID → String
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundError("User not found with id: " + id));

        if (newPassword.length() < 6) {
            throw new BadRequestError("Password must be at least 6 characters long");
        }

        if (newPassword.equals(user.getPassword())) {
            throw new BadRequestError("New password cannot be the same as the old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    @Override
    public void deactivateUser(String id) { // ← UUID → String
        // Soft delete user by setting isActive to false
        Optional<User> optUser = userRepository.findById(id);
        if (!optUser.isPresent()) {
            throw new NotFoundError("User not found with id: " + id);
        }
        User user = optUser.get();
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public void activateUser(String id) { // ← UUID → String
        // Soft delete user by setting isActive to false
        Optional<User> optUser = userRepository.findById(id);
        if (!optUser.isPresent()) {
            throw new NotFoundError("User not found with id: " + id);
        }
        User user = optUser.get();
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    public int getTotalUsers() {
        return (int) userRepository.count();
    }

    @Override
    public int getTotalMembers() {
        return userRepository.findAll().stream()
                .filter(user -> user instanceof Member)
                .mapToInt(user -> 1)
                .sum();
    }

    @Override
    public int getInactiveMembers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isActive())
                .mapToInt(user -> 1)
                .sum();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findById(String id) { // ← UUID → String
        return userRepository.findById(id);
    }
}
