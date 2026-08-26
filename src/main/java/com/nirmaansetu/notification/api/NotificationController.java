package com.nirmaansetu.notification.api;

import com.nirmaansetu.auth.application.AuthPrincipal;
import com.nirmaansetu.notification.application.NotificationService;
import com.nirmaansetu.notification.domain.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/api/notifications")
    public Page<NotificationResponse> getMyNotifications(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(required = false) NotificationStatus status,
            Pageable pageable) {
        return notificationService.getUserNotifications(principal.userId(), status, pageable);
    }

    @GetMapping("/api/notifications/unread-count")
    public Map<String, Long> getUnreadCount(@AuthenticationPrincipal AuthPrincipal principal) {
        long count = notificationService.getUnreadCount(principal.userId());
        return Map.of("unreadCount", count);
    }

    @PostMapping("/api/notifications/{id}/read")
    public NotificationResponse markAsRead(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID id) {
        return notificationService.markAsRead(principal.userId(), id);
    }

    @PostMapping("/api/notifications/read-all")
    public Map<String, Integer> markAllAsRead(@AuthenticationPrincipal AuthPrincipal principal) {
        return notificationService.markAllAsRead(principal.userId());
    }
}
