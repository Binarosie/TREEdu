package vn.hcmute.edu.authservice.config;

import java.util.List;
import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.hcmute.edu.authservice.enums.UserRole;
import vn.hcmute.edu.authservice.model.Permission;
import vn.hcmute.edu.authservice.model.Role;
import vn.hcmute.edu.authservice.repository.PermissionRepository;
import vn.hcmute.edu.authservice.repository.RoleRepository;

@Slf4j
@Component
@Order(1) // Run before UserDataSeeder
@RequiredArgsConstructor
public class RolePermissionDataInitializer implements ApplicationRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) {
        // 1) Ensure base permissions exist
        List<String> basePerms = List.of(
                "register", "login", "profile:read", "profile:update",
                // Roles CRUD
                "role:create", "role:read", "role:update", "role:delete",
                "role:grant", "role:revoke",
                // Permissions CRUD
                "perm:create", "perm:read", "perm:update", "perm:delete");

        for (String p : basePerms) {
            permissionRepository.findByName(p).orElseGet(() -> {
                log.info("[BOOTSTRAP] creating permission {}", p);
                return permissionRepository.save(Permission.builder().name(p).description(p).build());
            });
        }

        // 2) Ensure USER role exists
        roleRepository.findByName("USER").orElseGet(() -> {
            log.info("[BOOTSTRAP] creating role USER");
            return roleRepository.save(Role.builder()
                    .name("USER")
                    .description("Default user role")
                    .permissions(Set.of(
                            "register",
                            "login",
                            "profile:read",
                            "profile:update"))
                    .build());
        });

        // 3) Ensure SUPPORTER role exists
        roleRepository.findByName(UserRole.SUPPORTER.getRoleName()).orElseGet(() -> {
            log.info("[BOOTSTRAP] creating role SUPPORTER");
            return roleRepository.save(Role.builder()
                    .name(UserRole.SUPPORTER.getRoleName())
                    .description("Support staff with limited admin capabilities")
                    .permissions(Set.of(
                            "register",
                            "login",
                            "profile:read",
                            "profile:update",
                            "role:read",
                            "perm:read"))
                    .build());
        });

        // 4) Ensure ADMIN role exists
        roleRepository.findByName(UserRole.ADMIN.getRoleName()).orElseGet(() -> {
            log.info("[BOOTSTRAP] creating role ADMIN");
            return roleRepository.save(Role.builder()
                    .name(UserRole.ADMIN.getRoleName())
                    .description("Administrators")
                    .permissions(Set.of(
                            "register",
                            "login",
                            "profile:read",
                            "profile:update",
                            "role:create",
                            "role:read",
                            "role:update",
                            "role:delete",
                            "role:grant",
                            "role:revoke",
                            "perm:create",
                            "perm:read",
                            "perm:update",
                            "perm:delete"))
                    .build());
        });

        log.info("[BOOTSTRAP] role/permission seed complete");
    }
}