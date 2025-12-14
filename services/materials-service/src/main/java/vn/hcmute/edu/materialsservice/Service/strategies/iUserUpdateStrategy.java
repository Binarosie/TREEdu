package vn.hcmute.edu.materialsservice.Service.strategies;

import vn.hcmute.edu.materialsservice.Model.User;
import vn.hcmute.edu.materialsservice.Dto.request.users.CreateUserRequest;
import vn.hcmute.edu.materialsservice.Dto.request.users.UpdateProfileRequest;
import vn.hcmute.edu.materialsservice.Dto.request.users.UpdateUserRequest;

public interface iUserUpdateStrategy {
    boolean supports(User user);
    void update(User user, UpdateUserRequest request);

    // Optional: default empty, override only where needed
    default void updateProfile(User user, UpdateProfileRequest request) {
        throw new UnsupportedOperationException("This strategy does not support profile update.");
    }
}