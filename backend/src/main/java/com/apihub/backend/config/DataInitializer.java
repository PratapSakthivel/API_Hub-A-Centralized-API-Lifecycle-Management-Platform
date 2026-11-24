package com.apihub.backend.config;

import com.apihub.backend.features.users.domain.Role;
import com.apihub.backend.features.users.domain.RoleName;
import com.apihub.backend.features.users.domain.User;
import com.apihub.backend.features.users.repository.RoleRepository;
import com.apihub.backend.features.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.email:admin@apihub.local}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        // 1. Initialize all 5 standard roles
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder()
                        .name(roleName)
                        .description(roleName.name() + " role in API Hub")
                        .build());
            }
        }

        // 2. Ensure all demo role accounts exist with known passwords for 1-click evaluation
        seedUserIfNotExists(adminEmail, "System Administrator", "Admin@123456", RoleName.ADMIN, RoleName.DEVELOPER);
        seedUserIfNotExists("dev@apihub.local", "Elena Rostova (Developer)", "Dev@123456", RoleName.DEVELOPER);
        seedUserIfNotExists("pm@apihub.local", "Marcus Vance (Product Manager)", "Pm@123456", RoleName.PROJECT_MANAGER);
        seedUserIfNotExists("qa@apihub.local", "Sarah Connor (QA Engineer)", "Qa@123456", RoleName.QA_ENGINEER);
        seedUserIfNotExists("consumer@apihub.local", "David Kim (Partner Consumer)", "Consumer@123456", RoleName.CONSUMER);
    }

    private void seedUserIfNotExists(String email, String displayName, String password, RoleName... roles) {
        String normalizedEmail = email.toLowerCase().trim();
        if (userRepository.findByEmail(normalizedEmail).isEmpty()) {
            Set<Role> roleSet = new HashSet<>();
            for (RoleName r : roles) {
                roleRepository.findByName(r).ifPresent(roleSet::add);
            }
            User user = User.builder()
                    .email(normalizedEmail)
                    .displayName(displayName)
                    .passwordHash(passwordEncoder.encode(password))
                    .enabled(true)
                    .roles(roleSet)
                    .build();
            userRepository.save(user);
            log.info("Bootstrapped demo user: {} with roles {}", normalizedEmail, roleSet.stream().map(Role::getName).toList());
        }
    }
}
