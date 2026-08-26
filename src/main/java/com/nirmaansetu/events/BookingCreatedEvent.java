package com.nirmaansetu.events;

import java.util.UUID;

public record BookingCreatedEvent(
    UUID bookingId,
    UUID requirementId,
    UUID recipientUserId
) {}
