package vn.hcmute.edu.materialsservice.services.factories;

import vn.hcmute.edu.materialsservice.models.User;
import vn.hcmute.edu.materialsservice.models.Admin;
import vn.hcmute.edu.materialsservice.dtos.request.users.CreateUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminFactory implements iUserFactory {

    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(String userType) {
        return "ADMIN".equalsIgnoreCase(userType);
    }

    @Override
    public User createUser(CreateUserRequest request) {
        return Admin.builder()
                .id(java.util.UUID.randomUUID().toString())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .modifiedOn(LocalDateTime.now())
                .build();
    }
}