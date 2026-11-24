package com.apihub.backend.features.notifications.repository;

import com.apihub.backend.features.notifications.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId OR n.recipient IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findForUser(@Param("userId") Long userId);

    @Query("SELECT n FROM Notification n WHERE (n.recipient.id = :userId OR n.recipient IS NULL) AND n.read = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadForUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE (n.recipient.id = :userId OR n.recipient IS NULL) AND n.read = false")
    long countUnreadForUser(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE (n.recipient.id = :userId OR n.recipient IS NULL) AND n.read = false")
    int markAllAsReadForUser(@Param("userId") Long userId);

    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
