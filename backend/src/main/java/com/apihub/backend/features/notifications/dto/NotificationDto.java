package com.apihub.backend.features.notifications.dto;

import com.apihub.backend.features.notifications.domain.NotificationType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private Long id;
    private Long recipientId;
    private String recipientEmail;
    private String title;
    private String message;
    private NotificationType type;
    private String category;
    private boolean read;
    private String actionUrl;
    private Instant createdAt;
}
