package com.nirmaansetu.project.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectResponse(
    UUID id,
    UUID clientProfileId,
    String title,
    String description,
    String location,
    String status,
    LocalDate startDate,
    Instant createdAt,
    Instant updatedAt
) {}
