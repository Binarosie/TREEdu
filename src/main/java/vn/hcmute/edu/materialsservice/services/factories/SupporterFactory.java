package vn.hcmute.edu.materialsservice.services.factories;

import vn.hcmute.edu.materialsservice.models.User;
import vn.hcmute.edu.materialsservice.models.Supporter; // ← FIX: Đổi thành Supporter
import vn.hcmute.edu.materialsservice.dtos.request.users.CreateUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
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
        log.info("🔧 Creating SUPPORTER with email: {}", request.getEmail());

        Supporter supporter = Supporter.builder() // ← FIX: Đổi thành Supporter
                .id(UUID.randomUUID())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(false)
                .build();

        log.info(" Supporter created: {} (class: {})", supporter.getEmail(), supporter.getClass().getSimpleName());
        return supporter;
    }
}