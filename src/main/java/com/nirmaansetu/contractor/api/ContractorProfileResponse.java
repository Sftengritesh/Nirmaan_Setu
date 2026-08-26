package com.nirmaansetu.contractor.api;

import java.time.Instant;
import java.util.UUID;

public record ContractorProfileResponse(
    UUID id,
    UUID userId,
    String displayName,
    String description,
    String location,
    Instant createdAt,
    Instant updatedAt
) {}
