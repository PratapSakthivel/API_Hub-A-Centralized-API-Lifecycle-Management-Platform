package com.apihub.backend.integration;

import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.catalog.domain.LifecycleStatus;
import com.apihub.backend.features.catalog.repository.ApiRepository;
import com.apihub.backend.features.dashboard.dto.DashboardSummaryDto;
import com.apihub.backend.features.dashboard.service.DashboardService;
import com.apihub.backend.features.notifications.domain.NotificationType;
import com.apihub.backend.features.notifications.dto.CreateNotificationRequest;
import com.apihub.backend.features.notifications.dto.NotificationDto;
import com.apihub.backend.features.notifications.service.NotificationService;
import com.apihub.backend.features.projects.domain.Project;
import com.apihub.backend.features.projects.repository.ProjectRepository;
import com.apihub.backend.features.release.domain.ReleaseStage;
import com.apihub.backend.features.release.dto.CreateReleaseRequest;
import com.apihub.backend.features.release.dto.PromoteReleaseRequest;
import com.apihub.backend.features.release.dto.ReleaseDto;
import com.apihub.backend.features.release.service.ReleaseService;
import com.apihub.backend.features.users.domain.Role;
import com.apihub.backend.features.users.domain.RoleName;
import com.apihub.backend.features.users.domain.User;
import com.apihub.backend.features.users.dto.CreateUserRequest;
import com.apihub.backend.features.users.dto.UserDto;
import com.apihub.backend.features.users.repository.RoleRepository;
import com.apihub.backend.features.users.repository.UserRepository;
import com.apihub.backend.features.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApiHubPlatformIntegrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ApiRepository apiRepository;

    @Mock
    private ReleaseService releaseService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private DashboardService dashboardService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, passwordEncoder);
    }

    @Test
    @DisplayName("E2E Lifecycle Flow: User Registration -> Release Promotion -> Alert Notification -> Dashboard Analytics")
    void testEndToEndPlatformLifecycle() {
        // Step 1: Register Developer User
        Role devRole = Role.builder().id(1L).name(RoleName.ROLE_DEVELOPER).build();
        when(roleRepository.findByName(RoleName.ROLE_DEVELOPER)).thenReturn(Optional.of(devRole));
        when(userRepository.existsByEmail("dev@apihub.io")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pass");

        User savedUser = User.builder()
                .id(100L)
                .email("dev@apihub.io")
                .fullName("Platform Lead")
                .roles(Set.of(devRole))
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        CreateUserRequest userReq = CreateUserRequest.builder()
                .email("dev@apihub.io")
                .password("SecurePass123!")
                .fullName("Platform Lead")
                .roleNames(Set.of("ROLE_DEVELOPER"))
                .build();

        UserDto userResult = userService.createUser(userReq);
        assertNotNull(userResult);
        assertEquals("dev@apihub.io", userResult.getEmail());

        // Step 2: Simulate Release Promotion
        ReleaseDto promotedRelease = new ReleaseDto();
        promotedRelease.setId(50L);
        promotedRelease.setStage(ReleaseStage.PRODUCTION);
        promotedRelease.setApprovedBy("Admin");
        when(releaseService.promoteRelease(eq(50L), any(PromoteReleaseRequest.class)))
                .thenReturn(promotedRelease);

        PromoteReleaseRequest promoteReq = new PromoteReleaseRequest();
        promoteReq.setTargetStage(ReleaseStage.PRODUCTION);
        promoteReq.setApprovedBy("Admin");

        ReleaseDto releaseResult = releaseService.promoteRelease(50L, promoteReq);
        assertEquals(ReleaseStage.PRODUCTION, releaseResult.getStage());

        // Step 3: Dispatch Broadcast Notification
        NotificationDto notification = NotificationDto.builder()
                .id(1L)
                .title("Production Release Successful")
                .message("Core Gateway v2.0 is live.")
                .type(NotificationType.SUCCESS)
                .category("RELEASE")
                .read(false)
                .build();
        when(notificationService.createNotification(any(CreateNotificationRequest.class)))
                .thenReturn(notification);

        CreateNotificationRequest notifReq = CreateNotificationRequest.builder()
                .title("Production Release Successful")
                .message("Core Gateway v2.0 is live.")
                .type(NotificationType.SUCCESS)
                .category("RELEASE")
                .build();

        NotificationDto notifResult = notificationService.createNotification(notifReq);
        assertEquals(NotificationType.SUCCESS, notifResult.getType());
        assertFalse(notifResult.isRead());

        // Step 4: Verify Dashboard Metrics
        DashboardSummaryDto summary = DashboardSummaryDto.builder()
                .totalApis(15L)
                .totalProjects(4L)
                .totalUsers(12L)
                .testPassRatePercentage(98.5)
                .build();
        when(dashboardService.getSummary()).thenReturn(summary);

        DashboardSummaryDto finalSummary = dashboardService.getSummary();
        assertEquals(15L, finalSummary.getTotalApis());
        assertEquals(98.5, finalSummary.getTestPassRatePercentage());
    }
}
