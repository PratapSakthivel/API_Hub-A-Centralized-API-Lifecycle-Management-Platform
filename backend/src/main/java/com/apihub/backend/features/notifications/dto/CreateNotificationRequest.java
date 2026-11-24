package com.apihub.backend.features.notifications.dto;

import com.apihub.backend.features.notifications.domain.NotificationType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {
    private Long recipientId;
    private String title;
    private String message;
    private NotificationType type;
    private String category;
    private String actionUrl;
}
