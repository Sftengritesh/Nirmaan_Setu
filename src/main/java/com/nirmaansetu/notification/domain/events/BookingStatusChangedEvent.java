package com.nirmaansetu.notification.domain.events;

import java.util.UUID;

public record BookingStatusChangedEvent(
    UUID bookingId,
    String status,
    UUID recipientUserId
) {}
