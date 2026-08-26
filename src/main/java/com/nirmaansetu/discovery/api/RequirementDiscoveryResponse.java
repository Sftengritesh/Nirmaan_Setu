package com.nirmaansetu.discovery.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RequirementDiscoveryResponse(
    UUID id,
    UUID projectId,
    String location,
    LocalDate startDate,
    Integer durationDays,
    String workerType,
    UUID skillId,
    Integer quantity,
    BigDecimal dailyRate,
    BigDecimal budgetAmount,
    String currencyCode,
    Boolean accommodationAvailable,
    Boolean foodAvailable,
    String additionalNotes,
    String status,
    Instant createdAt
) {}
