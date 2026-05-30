package vn.hcmute.edu.materialsservice.services.strategies;

import vn.hcmute.edu.materialsservice.models.Member;
import vn.hcmute.edu.materialsservice.models.User;
import vn.hcmute.edu.materialsservice.dtos.request.users.UpdateProfileRequest;
import vn.hcmute.edu.materialsservice.dtos.request.users.UpdateUserRequest;
import vn.hcmute.edu.materialsservice.services.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberUpdateStrategy implements iUserUpdateStrategy {

    private final CloudinaryService cloudinaryService;

    @Override
    public boolean supports(User user) {
        return user instanceof Member;
    }

    @Override
    public void update(User user, UpdateUserRequest request) {
        Member member = (Member) user;
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            member.setFullName(request.getFullName());
        }
    }

    @Override
    public void updateProfile(User user, UpdateProfileRequest request) {
        Member member = (Member) user;

        // fullname: chỉ update nếu non-null và non-blank
        if (request.getFullname() != null && !request.getFullname().isBlank()) {
            member.setFullName(request.getFullname());
        }

        // phoneNumber: null = không đổi, "" = xóa số (set null vào DB)
        if (request.getPhoneNumber() != null) {
            member.setPhoneNumber(
                    request.getPhoneNumber().isBlank() ? null : request.getPhoneNumber());
        }

        // ← Xử lý upload file qua Cloudinary
        if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            // overwrite=true nên không cần xóa ảnh cũ
            String newAvatarUrl = cloudinaryService.uploadAvatar(
                    request.getAvatarFile(),
                    member.getId());
            member.setAvatarUrl(newAvatarUrl);

        } else if (request.getAvatarFile() != null && request.getAvatarFile().isEmpty()) {
            // Client gửi file rỗng có chủ ý = muốn xóa avatar
            cloudinaryService.deleteAvatar(member.getId());
            member.setAvatarUrl(null);
        }

        // birthYear: null = không đổi
        if (request.getBirthYear() != null) {
            member.setBirthYear(request.getBirthYear());
        }

        // address: null = không đổi, blank = xóa địa chỉ
        if (request.getAddress() != null) {
            member.setAddress(
                    request.getAddress().isBlank() ? null : request.getAddress());
        }

        // gender: null = không đổi, phải đúng enum (đã validate ở DTO)
        if (request.getGender() != null && !request.getGender().isBlank()) {
            member.setGender(request.getGender());
        }
    }
}
