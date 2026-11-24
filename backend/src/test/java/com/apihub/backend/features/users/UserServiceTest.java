package com.apihub.backend.features.users;

import com.apihub.backend.features.users.domain.Role;
import com.apihub.backend.features.users.domain.RoleName;
import com.apihub.backend.features.users.domain.User;
import com.apihub.backend.features.users.dto.CreateUserRequest;
import com.apihub.backend.features.users.dto.UserDto;
import com.apihub.backend.features.users.repository.RoleRepository;
import com.apihub.backend.features.users.repository.UserRepository;
import com.apihub.backend.features.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private Role developerRole;

    @BeforeEach
    void setUp() {
        developerRole = Role.builder().id(1L).name(RoleName.DEVELOPER).description("Developer").build();
    }

    @Test
    void createUser_Success() {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("dev@apihub.local");
        req.setDisplayName("Developer One");
        req.setPassword("Password123!");
        req.setRoles(Set.of(RoleName.DEVELOPER));

        when(userRepository.existsByEmail("dev@apihub.local")).thenReturn(false);
        when(roleRepository.findByName(RoleName.DEVELOPER)).thenReturn(Optional.of(developerRole));
        when(passwordEncoder.encode(any())).thenReturn("hashed_secret");

        User savedUser = User.builder()
                .id(10L)
                .email("dev@apihub.local")
                .displayName("Developer One")
                .passwordHash("hashed_secret")
                .enabled(true)
                .roles(Set.of(developerRole))
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto created = userService.createUser(req);

        assertNotNull(created);
        assertEquals("dev@apihub.local", created.getEmail());
        assertTrue(created.getRoles().contains(RoleName.DEVELOPER));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_DuplicateEmailThrowsException() {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("existing@apihub.local");
        when(userRepository.existsByEmail("existing@apihub.local")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(req));
    }
}
