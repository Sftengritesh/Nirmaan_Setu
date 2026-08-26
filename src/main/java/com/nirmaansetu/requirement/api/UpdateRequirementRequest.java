package com.nirmaansetu.requirement.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateRequirementRequest(
    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location must not exceed 255 characters")
    String location,

    @NotNull(message = "Start date is required")
    LocalDate startDate,

    @NotNull(message = "Duration days is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    Integer durationDays,

    @NotBlank(message = "Worker type is required")
    String workerType,

    @NotNull(message = "Skill ID is required")
    UUID skillId,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1 worker")
    Integer quantity,

    BigDecimal dailyRate,

    BigDecimal budgetAmount,

    String currencyCode,

    Boolean accommodationAvailable,

    Boolean foodAvailable,

    String additionalNotes
) {}
