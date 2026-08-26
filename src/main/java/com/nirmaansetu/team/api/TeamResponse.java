package com.nirmaansetu.team.api;

import java.time.Instant;
import java.util.UUID;

public record TeamResponse(
    UUID id,
    UUID managerUserId,
    String name,
    String description,
    String status,
    Instant createdAt,
    Instant updatedAt
) {}
