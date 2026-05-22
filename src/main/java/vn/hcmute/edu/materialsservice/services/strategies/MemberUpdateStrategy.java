package vn.hcmute.edu.materialsservice.services.strategies;

import vn.hcmute.edu.materialsservice.models.Member;
import vn.hcmute.edu.materialsservice.models.User;
import vn.hcmute.edu.materialsservice.dtos.request.users.UpdateProfileRequest;
import vn.hcmute.edu.materialsservice.dtos.request.users.UpdateUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberUpdateStrategy implements iUserUpdateStrategy {

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
                    request.getPhoneNumber().isBlank() ? null : request.getPhoneNumber()
            );
        }

        // avatarUrl: null = không đổi, "" = xóa ảnh (set null vào DB)
        if (request.getAvatarUrl() != null) {
            member.setAvatarUrl(
                    request.getAvatarUrl().isBlank() ? null : request.getAvatarUrl()
            );
        }

        // birthYear: null = không đổi
        if (request.getBirthYear() != null) {
            member.setBirthYear(request.getBirthYear());
        }

        // address: null = không đổi, blank = xóa địa chỉ
        if (request.getAddress() != null) {
            member.setAddress(
                    request.getAddress().isBlank() ? null : request.getAddress()
            );
        }

        // gender: null = không đổi, phải đúng enum (đã validate ở DTO)
        if (request.getGender() != null && !request.getGender().isBlank()) {
            member.setGender(request.getGender());
        }
    }
}
