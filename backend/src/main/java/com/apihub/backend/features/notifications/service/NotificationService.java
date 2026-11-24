package com.apihub.backend.features.notifications.service;

import com.apihub.backend.features.notifications.domain.Notification;
import com.apihub.backend.features.notifications.domain.NotificationType;
import com.apihub.backend.features.notifications.dto.CreateNotificationRequest;
import com.apihub.backend.features.notifications.dto.NotificationDto;
import com.apihub.backend.features.notifications.repository.NotificationRepository;
import com.apihub.backend.features.users.domain.User;
import com.apihub.backend.features.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(Long userId, boolean unreadOnly) {
        List<Notification> list;
        if (userId != null) {
            list = unreadOnly
                    ? notificationRepository.findUnreadForUser(userId)
                    : notificationRepository.findForUser(userId);
        } else {
            list = notificationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 50)).getContent();
            if (unreadOnly) {
                list = list.stream().filter(n -> !n.isRead()).collect(Collectors.toList());
            }
        }
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        if (userId != null) {
            return notificationRepository.countUnreadForUser(userId);
        }
        return notificationRepository.findAll().stream().filter(n -> !n.isRead()).count();
    }

    @Transactional
    public NotificationDto createNotification(CreateNotificationRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Notification title must not be blank.");
        }
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new IllegalArgumentException("Notification message must not be blank.");
        }

        User recipient = null;
        if (request.getRecipientId() != null) {
            recipient = userRepository.findById(request.getRecipientId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getRecipientId()));
        }

        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(request.getTitle().trim())
                .message(request.getMessage().trim())
                .type(request.getType() != null ? request.getType() : NotificationType.INFO)
                .category(request.getCategory() != null ? request.getCategory().toUpperCase() : "GENERAL")
                .actionUrl(request.getActionUrl())
                .read(false)
                .build();

        return toDto(notificationRepository.save(notification));
    }

    @Transactional
    public NotificationDto markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        notification.setRead(true);
        return toDto(notificationRepository.save(notification));
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        if (userId != null) {
            return notificationRepository.markAllAsReadForUser(userId);
        }
        List<Notification> unread = notificationRepository.findAll().stream()
                .filter(n -> !n.isRead())
                .peek(n -> n.setRead(true))
                .collect(Collectors.toList());
        notificationRepository.saveAll(unread);
        return unread.size();
    }

    private NotificationDto toDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .recipientId(n.getRecipient() != null ? n.getRecipient().getId() : null)
                .recipientEmail(n.getRecipient() != null ? n.getRecipient().getEmail() : "Broadcast")
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .category(n.getCategory())
                .read(n.isRead())
                .actionUrl(n.getActionUrl())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
