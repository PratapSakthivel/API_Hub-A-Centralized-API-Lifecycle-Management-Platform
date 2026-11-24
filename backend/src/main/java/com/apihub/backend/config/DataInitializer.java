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
        // Initialize standard roles
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder()
                        .name(roleName)
                        .description(roleName.name() + " role in API Hub")
                        .build());
            }
        }

        // Bootstrap initial administrator account safely if no users exist
        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByName(RoleName.ADMIN).orElseThrow();
            Role devRole = roleRepository.findByName(RoleName.DEVELOPER).orElseThrow();

            User admin = User.builder()
                    .email(adminEmail.toLowerCase().trim())
                    .displayName("System Administrator")
                    .passwordHash(passwordEncoder.encode("Admin@123456"))
                    .enabled(true)
                    .roles(new HashSet<>(Set.of(adminRole, devRole)))
                    .build();

            userRepository.save(admin);
            log.info("Bootstrapped default administrator account: {}", adminEmail);
        }
    }
}
