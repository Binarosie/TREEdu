package vn.hcmute.edu.materialsservice.Service.factories;

import vn.hcmute.edu.materialsservice.Model.User;
import vn.hcmute.edu.materialsservice.Dto.request.users.CreateUserRequest;

public interface iUserFactory {
    boolean supports(String userType);
    User createUser(CreateUserRequest request);
}