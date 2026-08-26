package com.nirmaansetu.worker.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkerProfileResponse(
    UUID id,
    UUID userId,
    String displayName,
    Short experienceYears,
    String location,
    String availabilityStatus,
    BigDecimal dailyRate,
    String profileDescription,
    boolean isTravelWilling,
    List<SkillResponse> skills,
    Instant createdAt,
    Instant updatedAt
) {
    public record SkillResponse(UUID id, String code, String name) {}
}
