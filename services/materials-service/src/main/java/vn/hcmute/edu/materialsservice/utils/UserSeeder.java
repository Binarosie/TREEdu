package vn.hcmute.edu.materialsservice.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.hcmute.edu.materialsservice.Model.Admin;
import vn.hcmute.edu.materialsservice.Model.Member;
import vn.hcmute.edu.materialsservice.Model.Supporter;
import vn.hcmute.edu.materialsservice.Model.User;
import vn.hcmute.edu.materialsservice.Repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.count() > 0) {
            return;
        }

        // ===== ADMIN =====
        Admin admin1 = Admin.builder()
                .id(UUID.randomUUID())
                .fullName("Admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("password123"))
                .isActive(true)
                .createdOn(LocalDateTime.of(2024, 1, 1, 10, 0))
                .modifiedOn(LocalDateTime.of(2024, 1, 1, 10, 0))
                .build();
        userRepository.save(admin1);

        // ===== MODERATOR =====
        Supporter moderator1 = Supporter.builder()
                .id(UUID.randomUUID())
                .fullName("Moderator 1")
                .email("mod1@email.com")
                .password(passwordEncoder.encode("password123"))
                .isActive(true)
                .createdOn(LocalDateTime.of(2024, 3, 3, 11, 11))
                .modifiedOn(LocalDateTime.of(2024, 3, 3, 11, 11))
                .build();
        userRepository.save(moderator1);


        // ===== MEMBER =====
        User member = new Member();
        member.setId(UUID.randomUUID());
        member.setFullName("Alice Nguyen");
        member.setEmail("alice@gmail.com");
        member.setPassword(passwordEncoder.encode("password123"));
        member.setActive(true);

        userRepository.save(member);

        // ===== MEMBER 2 =====
        User member2 = new Member();
        member2.setId(UUID.randomUUID());
        member2.setFullName("Bob Tran");
        member2.setEmail("bob@gmail.com");
        member2.setPassword(passwordEncoder.encode("password123"));
        member2.setActive(true);

        userRepository.save(member2);

        System.out.println("UserSeeder initialized users");
    }
}