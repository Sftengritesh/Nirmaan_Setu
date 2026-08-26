package com.nirmaansetu.client.api;

import java.time.Instant;
import java.util.UUID;

public record ClientProfileResponse(
    UUID id,
    UUID userId,
    String clientType,
    String displayName,
    Instant createdAt,
    Instant updatedAt
) {}
