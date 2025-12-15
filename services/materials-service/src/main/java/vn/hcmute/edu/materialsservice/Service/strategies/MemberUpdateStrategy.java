package vn.hcmute.edu.materialsservice.Service.strategies;

import vn.hcmute.edu.materialsservice.Model.Member;
import vn.hcmute.edu.materialsservice.Model.User;
import vn.hcmute.edu.materialsservice.Dto.request.users.CreateUserRequest;
import vn.hcmute.edu.materialsservice.Dto.request.users.UpdateProfileRequest;
import vn.hcmute.edu.materialsservice.Dto.request.users.UpdateUserRequest;
import vn.hcmute.edu.materialsservice.Dto.response.NotFoundError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

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