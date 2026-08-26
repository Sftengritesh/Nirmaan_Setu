package com.nirmaansetu.notification.infrastructure.persistence;

import com.nirmaansetu.notification.domain.NotificationStatus;
import com.nirmaansetu.notification.domain.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification")
public class NotificationEntity {

    @Id
    private UUID id;

    @Column(name = "recipient_user_id", nullable = false)
    private UUID recipientUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "reference_entity_type")
    private String referenceEntityType;

    @Column(name = "reference_entity_id")
    private UUID referenceEntityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected NotificationEntity() {}

    public NotificationEntity(UUID id, UUID recipientUserId, NotificationType type, String title,
                              String message, String referenceEntityType, UUID referenceEntityId, Instant now) {
        this.id = id;
        this.recipientUserId = recipientUserId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceEntityType = referenceEntityType;
        this.referenceEntityId = referenceEntityId;
        this.status = NotificationStatus.UNREAD;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getRecipientUserId() { return recipientUserId; }
    public NotificationType getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getReferenceEntityType() { return referenceEntityType; }
    public UUID getReferenceEntityId() { return referenceEntityId; }
    public NotificationStatus getStatus() { return status; }
    public Instant getReadAt() { return readAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void markAsRead(Instant now) {
        this.status = NotificationStatus.READ;
        this.readAt = now;
        this.updatedAt = now;
    }
}
