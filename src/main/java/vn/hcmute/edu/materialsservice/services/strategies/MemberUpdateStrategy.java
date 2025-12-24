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
        member.setFullName(request.getFullName());
    }

    @Override
    public void updateProfile(User user, UpdateProfileRequest request) {
        Member member = (Member) user;
        member.setFullName(request.getFullname());
    }
}