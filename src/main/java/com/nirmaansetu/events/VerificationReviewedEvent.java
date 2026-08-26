package com.nirmaansetu.events;

import java.util.UUID;

public record VerificationReviewedEvent(
    UUID verificationId,
    String status,
    UUID recipientUserId,
    String notes
) {}
