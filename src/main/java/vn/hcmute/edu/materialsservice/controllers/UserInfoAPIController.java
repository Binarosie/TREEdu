package vn.hcmute.edu.materialsservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hcmute.edu.materialsservice.dtos.MemberProfileDTO;
import vn.hcmute.edu.materialsservice.dtos.UserDetailDTO;
import vn.hcmute.edu.materialsservice.dtos.UserInfoDTO;
import vn.hcmute.edu.materialsservice.dtos.request.RegisterRequest;
import vn.hcmute.edu.materialsservice.dtos.request.users.CreateUserRequest;
import vn.hcmute.edu.materialsservice.dtos.request.users.UpdateProfileRequest;
import vn.hcmute.edu.materialsservice.dtos.request.users.UpdateUserRequest;
import vn.hcmute.edu.materialsservice.dtos.response.*;
import vn.hcmute.edu.materialsservice.Enum.EUserRole;
import vn.hcmute.edu.materialsservice.models.Admin;
import vn.hcmute.edu.materialsservice.models.Member;
import vn.hcmute.edu.materialsservice.models.Supporter;
import vn.hcmute.edu.materialsservice.models.User;
import vn.hcmute.edu.materialsservice.repository.UserRepository;
import vn.hcmute.edu.materialsservice.services.impl.UserServiceImpl;
import vn.hcmute.edu.materialsservice.services.specifications.UserSpecifications;
import vn.hcmute.edu.materialsservice.security.CustomUserDetails;
import vn.hcmute.edu.materialsservice.security.JwtTokenUtil;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserInfoAPIController {

    private final UserServiceImpl userService;

    private final UserRepository userRepository;

    private final UserSpecifications userSpecifications;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/newMember")
    public ResponseEntity<SuccessResponse> createUser(@Valid @RequestBody RegisterRequest request) {
        // Chuyển RegisterRequest → CreateUserRequest để truyền vào service
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setUserType("MEMBER");
        createRequest.setFullName(request.getFullName());
        createRequest.setEmail(request.getEmail());
        createRequest.setPassword(request.getPassword());
        createRequest.setPhoneNumber(request.getPhone());
        createRequest.setAvatarUrl(request.getAvatarUrl());
        createRequest.setBirthYear(request.getBirthYear());
        createRequest.setAddress(request.getAddress());
        createRequest.setGender(request.getGender());

        User user = userService.createMember(createRequest);
        return new ResponseEntity<>(new CreatedResponse("User created successfully", user), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping("/newSupporter")
    public ResponseEntity<SuccessResponse> createManager(@Valid @RequestBody CreateUserRequest request) {
        log.info("🎯 === CREATE SUPPORTER ENDPOINT CALLED ===");
        log.info("📋 Request Body:");
        log.info("  - userType: {}", request.getUserType());
        log.info("  - fullName: {}", request.getFullName());
        log.info("  - email: {}", request.getEmail());
        log.info("  - password: {}", request.getPassword() != null ? "***" : "null");

        User user = userService.createManager(request);

        log.info("✅ User created successfully:");
        log.info("  - ID: {}", user.getId());
        log.info("  - Class: {}", user.getClass().getSimpleName());
        log.info("  - Email: {}", user.getEmail());

        CreatedResponse response = new CreatedResponse("User created successfully", user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Lấy 1 user theo ID
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse> getUser(@PathVariable String id) { // ← UUID → String
        UserInfoDTO user = userService.getUserInfoById(id);
        if (user == null) {
            throw new NotFoundError("User not found");
        }
        SuccessResponse response = new SuccessResponse("User retrieved successfully", HttpStatus.OK.value(), user,
                LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
    @GetMapping("/user-detail/{userId}")
    public ResponseEntity<SuccessResponse> getUserDetail(@PathVariable String userId, // ← UUID → String
            Authentication authentication) {
        String trueUserId = getTrueUserId(userId, authentication);

        UserDetailDTO userInfo = userService.getUserDetailById(trueUserId);
        if (userInfo == null) {
            throw new NotFoundError("User not found");
        }
        SuccessResponse response = new SuccessResponse("User retrieved successfully", HttpStatus.OK.value(), userInfo,
                LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping({ "", "/" })
    public ResponseEntity<DataTableResponse<UserInfoDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "1") int draw,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "modifiedOn,desc") String[] sort) {
        System.out.println(">>> GET /api/users CALLED");
        try {
            // ===== 1. SORT =====
            Sort.Direction direction = Sort.Direction.DESC;
            String sortField = "modifiedOn";

            if (sort.length == 2) {
                sortField = sort[0];
                direction = Sort.Direction.fromString(sort[1]);
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            // ===== 2. QUERY MONGO (THAY SPECIFICATION) =====
            Page<User> usersPage = userSpecifications.searchUsers(
                    search,
                    role,
                    status,
                    pageable);

            // ===== 3. MAP TO DTO =====
            List<UserInfoDTO> userInfoList = usersPage.getContent().stream()
                    .map(user -> {
                        UserInfoDTO dto = new UserInfoDTO();
                        dto.setId(user.getId()); // ← Bỏ .toString() vì đã là String
                        dto.setEmail(user.getEmail());
                        dto.setName(user.getFullName());

                        // role
                        dto.setRole(
                                user instanceof Admin ? "Admin" : user instanceof Supporter ? "Supporter" : "Member");

                        // status
                        dto.setStatus(user.isActive() ? "Active" : "Inactive");
                        return dto;
                    })
                    .toList();

            // ===== 4. DATATABLE RESPONSE =====
            return ResponseEntity.ok(
                    new DataTableResponse<>(
                            draw,
                            usersPage.getTotalElements(),
                            usersPage.getTotalElements(),
                            userInfoList));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DataTableResponse<>(draw, 0, 0, List.of()));
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPPORTER', 'ROLE_MEMBER')")
    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> updateUser(
            @PathVariable String id, // ← UUID → String
            @RequestBody UpdateUserRequest request,
            Authentication authentication) {

        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = currentUserDetails.getUser();

        EUserRole currentUserRole = EUserRole.fromUser(currentUser);

        System.out.println("=== Update User Request ===");
        System.out.println("Current User: " + currentUser.getEmail());
        System.out.println("Current User Type: " + currentUser.getClass().getSimpleName());
        System.out.println("Current User Role: " + currentUserRole);
        System.out.println("Target User ID: " + id);

        if (currentUser.getId().equals(id)) { // ← ID so sánh trực tiếp (String)

            if (request.getRole() != null && !request.getRole().isBlank()) {
                throw new BadRequestError("Bạn không thể tự thay đổi role của mình");
            }

            userService.updateUserByID(id, request);

            return ResponseEntity.ok(new SuccessResponse(
                    "Cập nhật thông tin thành công",
                    200,
                    null,
                    LocalDateTime.now()));
        }

        if (currentUserRole == EUserRole.ADMIN) {
            System.out.println("Admin updating another user");

            userService.adminUpdateUser(id, request, currentUserRole);

            return ResponseEntity.ok(new SuccessResponse(
                    "Admin cập nhật user thành công",
                    200,
                    null,
                    LocalDateTime.now()));
        }

        System.out.println("Non-admin trying to update another user");
        throw new BadRequestError("Bạn không có quyền cập nhật thông tin user khác");
    }

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @PutMapping(value = "/update-my-profile/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> updateMyProfile(
            @PathVariable String id,
            @RequestPart(value = "fullname", required = false) String fullname,
            @RequestPart(value = "phoneNumber", required = false) String phoneNumber,
            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile,
            @RequestPart(value = "birthYear", required = false) String birthYear,
            @RequestPart(value = "address", required = false) String address,
            @RequestPart(value = "gender", required = false) String gender,
            Authentication authentication) {

        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = currentUserDetails.getUser();

        if (!currentUser.getId().equals(id)) {
            throw new BadRequestError("Bạn không có quyền chỉnh sửa hồ sơ của người khác!");
        }

        // Gom các part thành DTO
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullname(fullname);
        request.setPhoneNumber(phoneNumber);
        request.setAvatarFile(avatarFile);
        request.setBirthYear(birthYear != null ? Integer.valueOf(birthYear) : null);
        request.setAddress(address);
        request.setGender(gender);

        // 1. Thực hiện update dưới DB và nhận về object Member sau khi cập nhật
        Member member = (Member) userService.updateMyProfile(id, request);

        // 2. Chuyển đổi Entity sang DTO để giấu Password và Package Name đi
        MemberProfileDTO profileDTO = MemberProfileDTO.builder()
                .id(member.getId())
                .fullName(member.getFullName())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .avatarUrl(member.getAvatarUrl())
                .birthYear(member.getBirthYear())
                .address(member.getAddress())
                .gender(member.getGender())
                .streakCount(member.getStreakCount())
                .longestStreak(member.getLongestStreak())
                .xp(member.getXp())
                .level(member.getLevel())
                .totalQuizCompleted(member.getTotalQuizCompleted())
                .totalFlashcardLearned(member.getTotalFlashcardLearned())
                .lastStudyDate(member.getLastStudyDate())
                .build();

        // 3. Trả DTO về cho Postman/Frontend thay vì trả bừa Entity
        SuccessResponse response = new SuccessResponse(
                "User updated successfully",
                HttpStatus.OK.value(),
                profileDTO, // 🔥 Đã thay bằng DTO sạch đẹp
                LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    // Huỷ kích hoạt user
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> deactivateUser(@PathVariable String id,
            Authentication authentication) {
        String userId = getTrueUserId(id, authentication); // ← Bỏ UUID.fromString()

        userService.deactivateUser(userId);
        SuccessResponse response = new SuccessResponse("User deactivated successfully", HttpStatus.OK.value(), null,
                LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    // Kích hoạt user
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
    @PostMapping("/activate/{id}")
    public ResponseEntity<SuccessResponse> activateUser(@PathVariable String id,
            Authentication authentication) {
        String userId = getTrueUserId(id, authentication); // ← Bỏ UUID.fromString()

        userService.activateUser(userId);
        SuccessResponse response = new SuccessResponse("User activated successfully", HttpStatus.OK.value(), null,
                LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<SuccessResponse> getUserStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalUsers", userService.getTotalUsers());
        stats.put("totalMembers", userService.getTotalMembers());
        stats.put("inactiveMembers", userService.getInactiveMembers());
        SuccessResponse response = new SuccessResponse("User statistics retrieved successfully", HttpStatus.OK.value(),
                stats, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestParam("userId") String id,
            @RequestParam("newPassword") String newPassword,
            Authentication authentication) {
        String userId = getTrueUserId(id, authentication); // ← Bỏ UUID.fromString()

        SuccessResponse successResponse = new SuccessResponse(
                "Đổi mật khẩu thành công!", HttpStatus.OK.value(),
                userService.changePasswordById(userId, newPassword), LocalDateTime.now());
        return ResponseEntity.ok(successResponse);
    }

    public String getTrueUserId(String id, Authentication authentication) { // ← UUID → String
        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        var user = currentUserDetails.getUser();

        if (id == null || !(user instanceof Admin)) {
            return user.getId(); // ← Trả về String trực tiếp
        } else {
            return id; // ← Return String id directly
        }
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')")
    public ResponseEntity<MemberProfileDTO> getMyProfile(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails.getUser().getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundError("User not found"));

        if (!(user instanceof Member member)) {
            throw new BadRequestError("User is not a member");
        }

        // 📐 BIẾN SỐ GAMIFICATION TÍNH TOÁN TẠI ĐÂY
        int totalXp = member.getXp() != null ? member.getXp() : 0;

        // 🎯 Gọi UserService để lấy level chuẩn hóa theo công thức RPG chung
        int currentLevel = userService.calculateLevel(totalXp);

        // Thuật toán tính tiến trình: TotalXP = 50 * L * (L - 1)
        int xpFloorForCurrentLevel = 50 * currentLevel * (currentLevel - 1);
        int xpCeilForNextLevel = 50 * (currentLevel + 1) * currentLevel;
        int totalXpInThisLevelRange = xpCeilForNextLevel - xpFloorForCurrentLevel;

        int currentLevelProgressXp = totalXp - xpFloorForCurrentLevel;
        int xpNeededForNextLevel = xpCeilForNextLevel - totalXp;

        double progressPercentage = 0.0;
        if (totalXpInThisLevelRange > 0) {
            progressPercentage = ((double) currentLevelProgressXp / totalXpInThisLevelRange) * 100;
            progressPercentage = Math.round(progressPercentage * 10.0) / 10.0; // Làm tròn 1 chữ số thập phân (VD:
                                                                               // 45.5%)
        }

        MemberProfileDTO dto = MemberProfileDTO.builder()
                .id(member.getId())
                .fullName(member.getFullName())
                .email(member.getEmail())
                // === Field mới ===
                .phoneNumber(member.getPhoneNumber())
                .avatarUrl(member.getAvatarUrl())
                .birthYear(member.getBirthYear())
                .address(member.getAddress())
                .gender(member.getGender())
                // === Field Member ===
                .streakCount(member.getStreakCount())
                .longestStreak(member.getLongestStreak())
                .xp(totalXp)
                .level(currentLevel)
                .totalQuizCompleted(member.getTotalQuizCompleted())
                .totalFlashcardLearned(member.getTotalFlashcardLearned())
                .lastStudyDate(member.getLastStudyDate())
                // === 🚀 3 Field bổ trợ vẽ ProgressBar trên UI Frontend ===
                .xpNeededForNextLevel(xpNeededForNextLevel)
                .currentLevelProgressXp(currentLevelProgressXp)
                .progressPercentage(progressPercentage)
                .build();

        return ResponseEntity.ok(dto);
    }
}
