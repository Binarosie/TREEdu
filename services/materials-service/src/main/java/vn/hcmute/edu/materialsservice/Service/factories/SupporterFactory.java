package vn.hcmute.edu.materialsservice.Service.factories;

import vn.hcmute.edu.materialsservice.Model.User;
import vn.hcmute.edu.materialsservice.Model.Member;
import vn.hcmute.edu.materialsservice.Dto.request.users.CreateUserRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SupporterFactory implements iUserFactory {

    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(String userType) {
        return "SUPPORTER".equalsIgnoreCase(userType);
    }

    @Override
    public User createUser(CreateUserRequest request) {
        return Member.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(false)
                .build();
    }
}