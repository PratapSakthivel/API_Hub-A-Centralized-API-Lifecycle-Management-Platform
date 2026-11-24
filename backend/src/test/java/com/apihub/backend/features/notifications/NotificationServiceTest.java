package com.apihub.backend.features.notifications;

import com.apihub.backend.features.notifications.domain.Notification;
import com.apihub.backend.features.notifications.domain.NotificationType;
import com.apihub.backend.features.notifications.dto.CreateNotificationRequest;
import com.apihub.backend.features.notifications.dto.NotificationDto;
import com.apihub.backend.features.notifications.repository.NotificationRepository;
import com.apihub.backend.features.notifications.service.NotificationService;
import com.apihub.backend.features.users.domain.User;
import com.apihub.backend.features.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("lead@apihub.io")
                .fullName("Tech Lead")
                .build();
    }

    @Test
    void createNotification_Success() {
        CreateNotificationRequest req = CreateNotificationRequest.builder()
                .recipientId(1L)
                .title("API Version Deprecated")
                .message("Payments v1.0.0 has been deprecated by the security team.")
                .type(NotificationType.WARNING)
                .category("LIFECYCLE")
                .actionUrl("/apis/1/versions")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(10L);
            return n;
        });

        NotificationDto dto = notificationService.createNotification(req);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("API Version Deprecated", dto.getTitle());
        assertEquals(NotificationType.WARNING, dto.getType());
        assertEquals("LIFECYCLE", dto.getCategory());
        assertEquals("lead@apihub.io", dto.getRecipientEmail());
        assertFalse(dto.isRead());
    }

    @Test
    void markAsRead_Success() {
        Notification n = Notification.builder()
                .id(25L)
                .title("Test Alert")
                .message("Run failed")
                .read(false)
                .build();

        when(notificationRepository.findById(25L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationDto result = notificationService.markAsRead(25L);

        assertTrue(result.isRead());
    }

    @Test
    void getUnreadCount_ReturnsAccurateCount() {
        when(notificationRepository.countUnreadForUser(1L)).thenReturn(4L);

        long count = notificationService.getUnreadCount(1L);

        assertEquals(4L, count);
        verify(notificationRepository, times(1)).countUnreadForUser(1L);
    }
}
