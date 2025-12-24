package vn.hcmute.edu.materialsservice.services.factories;

import vn.hcmute.edu.materialsservice.models.User;
import vn.hcmute.edu.materialsservice.dtos.request.users.CreateUserRequest;

public interface iUserFactory {
    boolean supports(String userType);
    User createUser(CreateUserRequest request);
}