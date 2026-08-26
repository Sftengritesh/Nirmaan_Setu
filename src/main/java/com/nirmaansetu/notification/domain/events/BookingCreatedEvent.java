package com.nirmaansetu.notification.domain.events;

import java.util.UUID;

public record BookingCreatedEvent(
    UUID bookingId,
    UUID requirementId,
    UUID recipientUserId
) {}
