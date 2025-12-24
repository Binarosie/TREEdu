package vn.hcmute.edu.materialsservice.services.strategies;

import vn.hcmute.edu.materialsservice.dtos.request.users.UpdateUserRequest;
import vn.hcmute.edu.materialsservice.dtos.response.BadRequestError;
import vn.hcmute.edu.materialsservice.Enum.EUserRole;
import vn.hcmute.edu.materialsservice.models.Member;
import vn.hcmute.edu.materialsservice.models.Supporter;
import vn.hcmute.edu.materialsservice.models.User;
import vn.hcmute.edu.materialsservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Strategy dành cho ADMIN update thông tin user KHÁC
 */
@Component
@RequiredArgsConstructor
public class AdminUpdateOtherUserStrategy {

    private final UserRepository userRepository;

    /**
     * Admin update user khác
     * @param targetUser - User cần update
     * @param request - Thông tin update
     * @param currentUserRole - Role của người đang thực hiện update (từ JWT, dựa vào instance type)
     */
    public User updateByAdmin(User targetUser, UpdateUserRequest request, EUserRole currentUserRole) {
        // Kiểm tra người thực hiện có phải Admin không
        if (currentUserRole != EUserRole.ADMIN) {
            throw new BadRequestError("Chỉ Admin mới có quyền thực hiện hành động này");
        }

        // Lấy role hiện tại của target user
        EUserRole targetUserRole = EUserRole.fromUser(targetUser);

        // ❌ KHÔNG cho phép thay đổi thông tin của Admin khác
        if (targetUserRole == EUserRole.ADMIN) {
            throw new BadRequestError("Không thể thay đổi thông tin của Admin");
        }

        // Admin có thể update tên
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            targetUser.setFullName(request.getFullName());
        }

        // Admin có thể thay đổi role (MEMBER <-> SUPPORTER)
        if (request.getRole() != null && !request.getRole().isBlank()) {
            String newRoleStr = request.getRole().toUpperCase();

            // Validate role hợp lệ
            if (!newRoleStr.equals("MEMBER") && !newRoleStr.equals("SUPPORTER")) {
                throw new BadRequestError("Role chỉ có thể là MEMBER hoặc SUPPORTER");
            }

            EUserRole newRole = EUserRole.fromString(newRoleStr);

            // Nếu role thay đổi, cần tạo user mới với type khác
            if (newRole != targetUserRole) {
                System.out.println("🔄 Changing user type from " + targetUserRole + " to " + newRole);

                // Xóa user cũ và tạo user mới với type mới
                targetUser = changeUserType(targetUser, newRole);
            }
        }

        return targetUser;
    }

    /**
     * Thay đổi type của user (Member -> Supporter hoặc ngược lại)
     * Vì dùng Table Per Class inheritance, phải xóa record cũ và tạo record mới
     */
    private User changeUserType(User oldUser, EUserRole newRole) {
        // Lưu thông tin cần giữ lại
        String fullName = oldUser.getFullName();
        String email = oldUser.getEmail();
        String password = oldUser.getPassword();
        boolean isActive = oldUser.isActive();
        UUID oldId = oldUser.getId();

        // Xóa user cũ
        userRepository.delete(oldUser);
        // Đảm bảo xóa ngay lập tức

        // Tạo user mới với type mới
        User newUser;
        if (newRole == EUserRole.MEMBER) {
            newUser = Member.builder()
                    .id(oldId)
                    .fullName(fullName)
                    .email(email)
                    .password(password)
                    .isActive(isActive)
                    .build();
        } else if (newRole == EUserRole.SUPPORTER) {
            newUser = Supporter.builder()
                    .id(oldId)
                    .fullName(fullName)
                    .email(email)
                    .password(password)
                    .isActive(isActive)
                    .build();
        } else {
            throw new BadRequestError("Invalid role: " + newRole);
        }

        // Lưu user mới
        newUser = userRepository.save(newUser);

        System.out.println("✅ User type changed successfully. New type: " + newUser.getClass().getSimpleName());

        return newUser;
    }
}