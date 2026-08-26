package com.nirmaansetu.notification.application;

import com.nirmaansetu.notification.api.NotificationResponse;
import com.nirmaansetu.notification.domain.NotificationException;
import com.nirmaansetu.notification.domain.NotificationStatus;
import com.nirmaansetu.notification.domain.NotificationType;
import com.nirmaansetu.notification.infrastructure.persistence.NotificationEntity;
import com.nirmaansetu.notification.infrastructure.persistence.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {

    private static final int MAX_PAGE_SIZE = 50;

    private final NotificationRepository notificationRepository;
    private final Clock clock;

    public NotificationService(NotificationRepository notificationRepository, Clock clock) {
        this.notificationRepository = notificationRepository;
        this.clock = clock;
    }

    @Transactional
    public NotificationResponse createNotification(UUID recipientUserId, NotificationType type, String title,
                                                   String message, String referenceEntityType, UUID referenceEntityId) {
        if (recipientUserId == null) {
            throw new NotificationException("Recipient user ID cannot be null");
        }
        Instant now = clock.instant();
        NotificationEntity entity = new NotificationEntity(
                UUID.randomUUID(), recipientUserId, type, title, message, referenceEntityType, referenceEntityId, now
        );
        NotificationEntity saved = notificationRepository.save(entity);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(UUID recipientUserId, NotificationStatus status, Pageable pageable) {
        Pageable validPageable = sanitizePageable(pageable);
        Page<NotificationEntity> page;
        if (status != null) {
            page = notificationRepository.findByRecipientUserIdAndStatusOrderByCreatedAtDesc(recipientUserId, status, validPageable);
        } else {
            page = notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(recipientUserId, validPageable);
        }
        return page.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID recipientUserId) {
        return notificationRepository.countByRecipientUserIdAndStatus(recipientUserId, NotificationStatus.UNREAD);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID recipientUserId, UUID notificationId) {
        NotificationEntity entity = notificationRepository.findByIdAndRecipientUserId(notificationId, recipientUserId)
                .orElseThrow(() -> new NotificationException("Notification not found for user: " + notificationId));
        if (entity.getStatus() == NotificationStatus.UNREAD) {
            entity.markAsRead(clock.instant());
            entity = notificationRepository.save(entity);
        }
        return mapToResponse(entity);
    }

    @Transactional
    public Map<String, Integer> markAllAsRead(UUID recipientUserId) {
        List<NotificationEntity> unreadList = notificationRepository.findByRecipientUserIdAndStatus(recipientUserId, NotificationStatus.UNREAD);
        Instant now = clock.instant();
        for (NotificationEntity entity : unreadList) {
            entity.markAsRead(now);
        }
        notificationRepository.saveAll(unreadList);
        return Map.of("markedReadCount", unreadList.size());
    }

    private Pageable sanitizePageable(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            return PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort());
        }
        return pageable;
    }

    private NotificationResponse mapToResponse(NotificationEntity entity) {
        return new NotificationResponse(
                entity.getId(),
                entity.getRecipientUserId(),
                entity.getType().name(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getReferenceEntityType(),
                entity.getReferenceEntityId(),
                entity.getStatus().name(),
                entity.getReadAt(),
                entity.getCreatedAt()
        );
    }
}
