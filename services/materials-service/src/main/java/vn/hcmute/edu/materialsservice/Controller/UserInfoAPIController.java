package vn.hcmute.edu.materialsservice.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.Dto.UserDetailDTO;
import vn.hcmute.edu.materialsservice.Dto.UserInfoDTO;
import vn.hcmute.edu.materialsservice.Dto.request.users.CreateUserRequest;
import vn.hcmute.edu.materialsservice.Dto.request.users.UpdateProfileRequest;
import vn.hcmute.edu.materialsservice.Dto.request.users.UpdateUserRequest;
import vn.hcmute.edu.materialsservice.Dto.response.*;
import vn.hcmute.edu.materialsservice.Enum.EUserRole;
import vn.hcmute.edu.materialsservice.Model.Admin;
import vn.hcmute.edu.materialsservice.Model.Member;
import vn.hcmute.edu.materialsservice.Model.Supporter;
import vn.hcmute.edu.materialsservice.Model.User;
import vn.hcmute.edu.materialsservice.Repository.UserRepository;
import vn.hcmute.edu.materialsservice.Service.Impl.UserServiceImpl;
import vn.hcmute.edu.materialsservice.Service.specifications.UserSpecifications;
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
    public ResponseEntity<SuccessResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createMember(request);
        CreatedResponse response = new CreatedResponse("User created successfully", user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

//    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
//    @PostMapping("/newSupporter")
//    public ResponseEntity<SuccessResponse> createManager(@Valid @RequestBody CreateUserRequest request) {
//        User user = userService.createManager(request);
//        CreatedResponse response = new CreatedResponse("User created successfully", user);
//        return new ResponseEntity<>(response, HttpStatus.CREATED);
//    }

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
    public ResponseEntity<SuccessResponse> getUser(@PathVariable UUID id) {
        UserInfoDTO user = userService.getUserInfoById(id);
        if( user == null) {
            throw new NotFoundError("User not found");
        }
        SuccessResponse response = new SuccessResponse("User retrieved successfully", HttpStatus.OK.value(), user, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
    @GetMapping("/user-detail/{userId}")
    public ResponseEntity<SuccessResponse> getUserDetail(@PathVariable UUID userId,
                                                         Authentication authentication) {
        UUID trueUserId = getTrueUserId(userId, authentication);

        UserDetailDTO userInfo = userService.getUserDetailById(trueUserId);
        if( userInfo == null) {
            throw new NotFoundError("User not found");
        }
        SuccessResponse response = new SuccessResponse("User retrieved successfully", HttpStatus.OK.value(), userInfo, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping({"", "/"})
    public ResponseEntity<DataTableResponse<UserInfoDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "1") int draw,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "modifiedOn,desc") String[] sort
    ) {
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
                    pageable
            );

            // ===== 3. MAP TO DTO =====
            List<UserInfoDTO> userInfoList = usersPage.getContent().stream()
                    .map(user -> {
                        UserInfoDTO dto = new UserInfoDTO();
                        dto.setId(user.getId().toString());
                        dto.setEmail(user.getEmail());
                        dto.setName(user.getFullName());

                        // role
                        dto.setRole(
                                user instanceof Admin ? "Admin" :
                                        user instanceof Supporter ? "Supporter" :
                                                "Member"
                        );

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
                            userInfoList
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DataTableResponse<>(draw, 0, 0, List.of()));
        }
    }


//    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
//    @PutMapping("/{id}")
//    public ResponseEntity<SuccessResponse> updateUser(@PathVariable UUID id,
//                                                      @ModelAttribute UpdateUserRequest request,
//                                                      Authentication authentication) {
//        UUID trueUserId = getTrueUserId(id, authentication);
//        userService.updateUserByID(trueUserId, request);
//        return ResponseEntity.ok(new SuccessResponse("User updated successfully", 200, null, LocalDateTime.now()));
//    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPPORTER', 'ROLE_MEMBER')")
    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> updateUser(
            @PathVariable UUID id,
            @RequestBody UpdateUserRequest request,
            Authentication authentication) {

        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = currentUserDetails.getUser();

        EUserRole currentUserRole = EUserRole.fromUser(currentUser);  // ← Sử dụng ở đây

        System.out.println("=== Update User Request ===");
        System.out.println("Current User: " + currentUser.getEmail());
        System.out.println("Current User Type: " + currentUser.getClass().getSimpleName());
        System.out.println("Current User Role: " + currentUserRole);
        System.out.println("Target User ID: " + id);

        if (currentUser.getId().equals(id)) {
            System.out.println(" User updating themselves");

            if (request.getRole() != null && !request.getRole().isBlank()) {
                throw new BadRequestError("Bạn không thể tự thay đổi role của mình");
            }

            userService.updateUserByID(id, request);

            return ResponseEntity.ok(new SuccessResponse(
                    "Cập nhật thông tin thành công",
                    200,
                    null,
                    LocalDateTime.now()
            ));
        }

        if (currentUserRole == EUserRole.ADMIN) {
            System.out.println("Admin updating another user");

            userService.adminUpdateUser(id, request, currentUserRole);

            return ResponseEntity.ok(new SuccessResponse(
                    "Admin cập nhật user thành công",
                    200,
                    null,
                    LocalDateTime.now()
            ));
        }

        System.out.println("Non-admin trying to update another user");
        throw new BadRequestError("Bạn không có quyền cập nhật thông tin user khác");
    }


    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @PutMapping("/update-my-profile/{id}")
    public ResponseEntity<SuccessResponse> updateMyProfile(@PathVariable UUID id, @RequestBody UpdateProfileRequest request) {
        User user = userService.updateMyProfile(id, request);
        SuccessResponse response = new SuccessResponse("User updated successfully", HttpStatus.OK.value(), user, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    // Huỷ kích hoạt user
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> deactivateUser(@PathVariable String id,
                                                          Authentication authentication) {
        UUID userId = getTrueUserId(UUID.fromString(id), authentication);

        userService.deactivateUser(userId);
        SuccessResponse response = new SuccessResponse("User deactivated successfully", HttpStatus.OK.value(), null, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    // Kích hoạt user
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
    @PostMapping("/activate/{id}")
    public ResponseEntity<SuccessResponse> activateUser(@PathVariable String id,
                                                        Authentication authentication) {
        UUID userId = getTrueUserId(UUID.fromString(id), authentication);

        userService.activateUser(userId);
        SuccessResponse response = new SuccessResponse("User activated successfully", HttpStatus.OK.value(), null, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<SuccessResponse> getUserStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalUsers", userService.getTotalUsers());
        stats.put("totalMembers", userService.getTotalMembers());
        stats.put("inactiveMembers", userService.getInactiveMembers());
        SuccessResponse response = new SuccessResponse("User statistics retrieved successfully", HttpStatus.OK.value(), stats, LocalDateTime.now());
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestParam("userId") String id,
                                            @RequestParam("newPassword") String newPassword,
                                            Authentication authentication) {
        UUID userId = getTrueUserId(UUID.fromString(id), authentication);

        SuccessResponse successResponse = new SuccessResponse(
                "Đổi mật khẩu thành công!", HttpStatus.OK.value(),
                userService.changePasswordById(userId, newPassword), LocalDateTime.now());
        return ResponseEntity.ok(successResponse);
    }

    public UUID getTrueUserId(UUID id, Authentication authentication) {
        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        var user = currentUserDetails.getUser();

        if(id == null || !(user instanceof Admin)) {
            return user.getId();
        } else {
            return id;
        }
    }
}