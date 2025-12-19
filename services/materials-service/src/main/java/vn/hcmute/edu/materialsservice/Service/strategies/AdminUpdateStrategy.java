package vn.hcmute.edu.materialsservice.Service.strategies;
import vn.hcmute.edu.materialsservice.Dto.request.users.UpdateProfileRequest;
import vn.hcmute.edu.materialsservice.Dto.request.users.UpdateUserRequest;
import vn.hcmute.edu.materialsservice.Dto.response.BadRequestError;
import vn.hcmute.edu.materialsservice.Model.Admin;
import vn.hcmute.edu.materialsservice.Model.User;
import org.springframework.stereotype.Component;

@Component
public class AdminUpdateStrategy implements iUserUpdateStrategy {

    @Override
    public boolean supports(User user) {
        return user instanceof Admin;
    }

    @Override
    public void update(User user, UpdateUserRequest request) {
        Admin admin = (Admin) user;

        // Admin chỉ có thể update tên của chính mình
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            admin.setFullName(request.getFullName());
        }

        // Admin KHÔNG thể thay đổi role của chính mình
        // (để tránh tự hạ quyền hoặc lock toàn bộ hệ thống)
    }

    @Override
    public void updateProfile(User user, UpdateProfileRequest request) {
        Admin admin = (Admin) user;
        if (request.getFullname() != null && !request.getFullname().isBlank()) {
            admin.setFullName(request.getFullname());
        }
    }
}