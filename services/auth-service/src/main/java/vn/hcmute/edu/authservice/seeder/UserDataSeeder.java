package vn.hcmute.edu.authservice.seeder;

import java.time.Instant;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.hcmute.edu.authservice.enums.UserRole;
import vn.hcmute.edu.authservice.model.User;
import vn.hcmute.edu.authservice.repository.UserRepository;

@Slf4j
@Component
@Order(2) // Run after RolePermissionDataInitializer
@RequiredArgsConstructor
public class UserDataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${auth.init.enabled:true}")
    private boolean initEnabled;

    @Override
    public void run(ApplicationArguments args) {
        if (!initEnabled) {
            log.info("[SEEDER] User data seeding is disabled");
            return;
        }

        log.info("[SEEDER] Starting user data seeding...");

        // Seed ADMIN user
        seedUser(
                "admin",
                "admin@treedu.edu.vn",
                "Admin@123456",
                Set.of(UserRole.ADMIN.getRoleName()),
                "Administrator");

        // Seed SUPPORTER user
        seedUser(
                "supporter01",
                "supporter@treedu.edu.vn",
                "Supporter@123456",
                Set.of(UserRole.SUPPORTER.getRoleName()),
                "Support Staff");

        // Seed MEMBER user
        seedUser(
                "member01",
                "member@treedu.edu.vn",
                "Member@123456",
                Set.of(UserRole.MEMBER.getRoleName()),
                "Test default Member");

        log.info("[SEEDER] User data seeding completed");
    }

    private void seedUser(String username, String email, String password, Set<String> roles, String description) {
        if (userRepository.existsByUsername(username)) {
            log.info("[SEEDER] User '{}' already exists, skipping...", username);
            return;
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(roles)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        userRepository.save(user);
        log.info("[SEEDER] Created user '{}' ({}) with roles: {}", username, description, roles);
    }
}
