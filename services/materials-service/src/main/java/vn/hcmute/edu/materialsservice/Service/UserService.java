package vn.hcmute.edu.materialsservice.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.hcmute.edu.materialsservice.Dto.request.LoginRequest;
import vn.hcmute.edu.materialsservice.Dto.request.RegisterRequest;
import vn.hcmute.edu.materialsservice.Dto.request.UserRequest;
import vn.hcmute.edu.materialsservice.Dto.response.AuthResponse;
import vn.hcmute.edu.materialsservice.Dto.response.UserResponse;

public interface UserService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getUserById(String id);

    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse updateUser(String id, UserRequest request);

    void deleteUser(String id);

    UserResponse getMyInfo();
}
