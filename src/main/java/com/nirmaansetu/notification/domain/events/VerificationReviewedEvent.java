package com.nirmaansetu.notification.domain.events;

import java.util.UUID;

public record VerificationReviewedEvent(
    UUID verificationId,
    String status,
    UUID recipientUserId,
    String notes
) {}
