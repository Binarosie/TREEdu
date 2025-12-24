package vn.hcmute.edu.materialsservice.services.strategies;

import vn.hcmute.edu.materialsservice.models.Supporter;
import vn.hcmute.edu.materialsservice.models.User;
import vn.hcmute.edu.materialsservice.dtos.request.users.UpdateUserRequest;
import org.springframework.stereotype.Component;

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