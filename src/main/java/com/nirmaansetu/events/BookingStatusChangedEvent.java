package com.nirmaansetu.events;

import java.util.UUID;

public record BookingStatusChangedEvent(
    UUID bookingId,
    String status,
    UUID recipientUserId
) {}
