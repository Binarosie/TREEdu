package vn.hcmute.edu.materialsservice.Service.strategies;

import vn.hcmute.edu.materialsservice.Model.Supporter;
import vn.hcmute.edu.materialsservice.Model.User;
import vn.hcmute.edu.materialsservice.Dto.request.users.UpdateUserRequest;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SupporterUpdateStrategy implements iUserUpdateStrategy {
    @Override
    public boolean supports(User user) {
        return user instanceof Supporter;
    }

    @Override
    public void update(User user, UpdateUserRequest request) {
        Supporter moderator = (Supporter) user;
        moderator.setFullName(request.getFullName());
    }
}