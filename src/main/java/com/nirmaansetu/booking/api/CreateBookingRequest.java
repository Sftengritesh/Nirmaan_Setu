package com.nirmaansetu.booking.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateBookingRequest(
    @NotBlank(message = "Provider type is required")
    String providerType,

    UUID providerWorkerProfileId,

    UUID providerTeamId,

    UUID providerContractorProfileId,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity
) {}
