package com.plr.aduaja.controller;

import com.plr.aduaja.model.Notification;
import com.plr.aduaja.model.Notification.NotificationType;
import com.plr.aduaja.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class NotificationApiController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUser(userId));
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotificationsByUser(userId));
    }

    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.countUnreadByUser(userId));
    }

    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestParam String userId,
                                                            @RequestParam String message,
                                                            @RequestParam NotificationType type) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    notificationService.createNotification(userId, message, type));
        } catch (Exception e) {
            log.error("Gagal buat notifikasi: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable String id) {
        try {
            return ResponseEntity.ok(notificationService.markAsRead(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable String userId) {
        try {
            notificationService.markAllAsReadByUser(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Gagal mark all read {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
