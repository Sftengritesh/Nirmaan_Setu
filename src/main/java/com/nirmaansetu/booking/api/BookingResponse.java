package com.nirmaansetu.booking.api;

import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
    UUID id,
    UUID requirementId,
    String providerType,
    UUID providerWorkerProfileId,
    UUID providerTeamId,
    UUID providerContractorProfileId,
    Integer quantity,
    String status,
    Instant requestedAt,
    Instant respondedAt,
    Instant createdAt,
    Instant updatedAt
) {}
