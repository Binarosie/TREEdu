package vn.hcmute.edu.materialsservice.services.strategies;

import vn.hcmute.edu.materialsservice.models.User;
import vn.hcmute.edu.materialsservice.dtos.request.users.UpdateProfileRequest;
import vn.hcmute.edu.materialsservice.dtos.request.users.UpdateUserRequest;

public interface iUserUpdateStrategy {
    boolean supports(User user);
    void update(User user, UpdateUserRequest request);

    // Optional: default empty, override only where needed
    default void updateProfile(User user, UpdateProfileRequest request) {
        throw new UnsupportedOperationException("This strategy does not support profile update.");
    }
}