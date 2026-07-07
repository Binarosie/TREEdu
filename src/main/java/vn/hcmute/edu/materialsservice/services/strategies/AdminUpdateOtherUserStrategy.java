package vn.hcmute.edu.materialsservice.services.strategies;

import vn.hcmute.edu.materialsservice.dtos.request.users.UpdateUserRequest;
import vn.hcmute.edu.materialsservice.dtos.response.BadRequestError;
import vn.hcmute.edu.materialsservice.Enum.EUserRole;
import vn.hcmute.edu.materialsservice.models.Member;
import vn.hcmute.edu.materialsservice.models.NotificationEvent;
import vn.hcmute.edu.materialsservice.models.Supporter;
import vn.hcmute.edu.materialsservice.models.User;
import vn.hcmute.edu.materialsservice.repository.UserRepository;
import vn.hcmute.edu.materialsservice.services.observer.NotificationCenter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminUpdateOtherUserStrategy {

    private final UserRepository userRepository;

    public User updateByAdmin(User targetUser, UpdateUserRequest request, EUserRole currentUserRole) {
        if (currentUserRole != EUserRole.ADMIN) {
            throw new BadRequestError("Chỉ Admin mới có quyền thực hiện hành động này");
        }

        EUserRole targetUserRole = EUserRole.fromUser(targetUser);

        if (targetUserRole == EUserRole.ADMIN) {
            throw new BadRequestError("Không thể thay đổi thông tin của Admin");
        }

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            targetUser.setFullName(request.getFullName());
        }

        // Đổi role MEMBER <-> SUPPORTER (giữ nguyên logic cũ)
        if (request.getRole() != null && !request.getRole().isBlank()) {
            String newRoleStr = request.getRole().toUpperCase();
            if (!newRoleStr.equals("MEMBER") && !newRoleStr.equals("SUPPORTER")) {
                throw new BadRequestError("Role chỉ có thể là MEMBER hoặc SUPPORTER");
            }
            EUserRole newRole = EUserRole.fromString(newRoleStr);
            if (newRole != targetUserRole) {
                targetUser = changeUserType(targetUser, newRole);
            }
        }

        // chỉ áp dụng khi targetUser (hiện tại) là Member
        if (targetUser instanceof Member member) {
            StringBuilder reasonBuilder = new StringBuilder();
            boolean permissionChanged = false;

            if (request.getCanPublishFlashcard() != null
                    && !request.getCanPublishFlashcard().equals(member.getCanPublishFlashcard())) {
                member.setCanPublishFlashcard(request.getCanPublishFlashcard());
                permissionChanged = true;
                reasonBuilder.append(Boolean.TRUE.equals(request.getCanPublishFlashcard())
                        ? "Bạn đã được cấp lại quyền công khai flashcard. "
                        : "Quyền công khai flashcard của bạn đã tạm bị khoá do hệ thống ghi nhận dấu hiệu bất thường/spam. ");
            }

            if (request.getCanReportFlashcard() != null
                    && !request.getCanReportFlashcard().equals(member.getCanReportFlashcard())) {
                member.setCanReportFlashcard(request.getCanReportFlashcard());
                permissionChanged = true;
                reasonBuilder.append(Boolean.TRUE.equals(request.getCanReportFlashcard())
                        ? "Bạn đã được cấp lại quyền báo cáo flashcard. "
                        : "Quyền báo cáo flashcard của bạn đã tạm bị khoá do hệ thống ghi nhận dấu hiệu bất thường/spam. ");
            }

            if (permissionChanged) {
                NotificationCenter.notifyObservers(NotificationEvent.builder()
                        .receiverId(member.getId())
                        .type("PERMISSION_CHANGED")
                        .title("Cập nhật quyền tài khoản")
                        .content(reasonBuilder
                                + "Nếu bạn thấy đây là nhầm lẫn, vui lòng gửi phản hồi/kháng cáo tới quản trị viên.")
                        .build());
            }
        }

        // Trước đây chỗ này KHÔNG save() khi không đổi role -> fullName/quyền sẽ mất.
        // Bổ sung save để đảm bảo mọi thay đổi (fullName, 2 quyền) được lưu lại.
        return userRepository.save(targetUser);
    }

    private User changeUserType(User oldUser, EUserRole newRole) {
        String fullName = oldUser.getFullName();
        String email = oldUser.getEmail();
        String password = oldUser.getPassword();
        boolean isActive = oldUser.isActive();
        String oldId = oldUser.getId();

        userRepository.delete(oldUser);

        User newUser;
        if (newRole == EUserRole.MEMBER) {
            newUser = Member.builder()
                    .id(oldId)
                    .fullName(fullName)
                    .email(email)
                    .password(password)
                    .isActive(isActive)
                    .canPublishFlashcard(true)
                    .canReportFlashcard (true)
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

        return userRepository.save(newUser);
    }
}