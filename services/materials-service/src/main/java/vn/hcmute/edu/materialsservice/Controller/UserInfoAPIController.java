package vn.hcmute.edu.materialsservice.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import vn.hcmute.edu.materialsservice.Dto.response.CreatedResponse;
import vn.hcmute.edu.materialsservice.Dto.response.DataTableResponse;
import vn.hcmute.edu.materialsservice.Dto.response.NotFoundError;
import vn.hcmute.edu.materialsservice.Dto.response.SuccessResponse;
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

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping("/newSupporter")
    public ResponseEntity<SuccessResponse> createManager(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createManager(request);
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
    @GetMapping
    public ResponseEntity<DataTableResponse<UserInfoDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "1") int draw,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "modifiedOn,desc") String[] sort
    ) {

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
                        dto.setId(user.getUserId().toString());
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


    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> updateUser(@PathVariable UUID id,
                                                      @ModelAttribute UpdateUserRequest request,
                                                      Authentication authentication) {
        UUID trueUserId = getTrueUserId(id, authentication);
        userService.updateUserByID(trueUserId, request);
        return ResponseEntity.ok(new SuccessResponse("User updated successfully", 200, null, LocalDateTime.now()));
    }


    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @PutMapping("/updateMyProfile")
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
        SuccessResponse response = new SuccessResponse("User deactivated successfully", HttpStatus.OK.value(), null, LocalDateTime.now());
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
            return user.getUserId();
        } else {
            return id;
        }
    }
}