package vn.hcmute.edu.materialsservice.Service.Impl;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.materialsservice.Dto.UserDetailDTO;
import vn.hcmute.edu.materialsservice.Dto.UserInfoDTO;
import vn.hcmute.edu.materialsservice.Dto.request.users.CreateUserRequest;
import vn.hcmute.edu.materialsservice.Dto.request.users.UpdateProfileRequest;
import vn.hcmute.edu.materialsservice.Dto.request.users.UpdateUserRequest;
import vn.hcmute.edu.materialsservice.Dto.response.BadRequestError;
import vn.hcmute.edu.materialsservice.Dto.response.ConflictError;
import vn.hcmute.edu.materialsservice.Dto.response.InternalServerError;
import vn.hcmute.edu.materialsservice.Dto.response.NotFoundError;
import vn.hcmute.edu.materialsservice.Model.Member;
import vn.hcmute.edu.materialsservice.Model.User;
import vn.hcmute.edu.materialsservice.Repository.UserRepository;
import vn.hcmute.edu.materialsservice.Service.EmailService;
import vn.hcmute.edu.materialsservice.Service.factories.iUserFactory;
import vn.hcmute.edu.materialsservice.Service.iUserService;
import vn.hcmute.edu.materialsservice.Service.strategies.iUserUpdateStrategy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements iUserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final List<iUserFactory> userFactories;
    private final List<iUserUpdateStrategy> updateStrategies;

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

        try {// ≥≤>
             // random 6 chữ số
            int code = (int) ((Math.random() * 900000) + 100000);
            String verificationCode = String.valueOf(code);

            // Gửi email
            emailService.sendVerificationEmail(user.getEmail(), verificationCode);
        } catch (MessagingException e) {
            throw new InternalServerError("Could not send verification email");
        }

        return userRepository.save(user);
    }

    public User createOAuthMember(String email, String fullName) {
        // FIX: Tìm user trước, chỉ tạo mới nếu chưa tồn tại
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

    @Override
    public User createManager(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictError("User already exists with email: " + request.getEmail());
        }

        iUserFactory factory = getFactory(request.getUserType());
        User user = factory.createUser(request);
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public UserInfoDTO getUserInfoById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundError("User not found with id: " + id));

        UserInfoDTO dto = new UserInfoDTO();
        dto = dto.mapToUserInfo(user);
        return dto;
    }

    @Override
    public UserDetailDTO getUserDetailById(UUID id) {
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
    public boolean existsByUserIdAndIsActive(UUID userId, boolean isActive) {
        return userRepository.existsByUserIdAndIsActive(userId, isActive);
    }

    // Dùng lại CreateUserRequest để update
    @Override
    public User updateMyProfile(UUID id, UpdateProfileRequest request) {
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
    public User updateUserByID(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundError("User not found with id: " + id));

        getUpdateStrategy(user).update(user, request);
        return userRepository.save(user);
    }

    @Override
    public boolean changePasswordById(UUID id, String newPassword) {
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
    public void deactivateUser(UUID id) {
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
    public void activateUser(UUID id) {
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
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }
}
