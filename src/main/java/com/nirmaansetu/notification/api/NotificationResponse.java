package com.nirmaansetu.notification.api;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    UUID recipientUserId,
    String type,
    String title,
    String message,
    String referenceEntityType,
    UUID referenceEntityId,
    String status,
    Instant readAt,
    Instant createdAt
) {}
