package com.nirmaansetu.worker.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateWorkerProfileRequest(
    @NotBlank @Size(max = 120) String displayName,
    @NotBlank @Size(max = 255) String location,
    @NotNull String availabilityStatus,
    @Min(0) Short experienceYears,
    @DecimalMin("0.0") BigDecimal dailyRate,
    @Size(max = 2000) String profileDescription,
    boolean isTravelWilling
) {}
