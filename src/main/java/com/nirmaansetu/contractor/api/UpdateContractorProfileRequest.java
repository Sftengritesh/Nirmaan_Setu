package com.nirmaansetu.contractor.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateContractorProfileRequest(
    @NotBlank(message = "Display name is required.")
    @Size(max = 160, message = "Display name must not exceed 160 characters.")
    String displayName,

    @NotBlank(message = "Location is required.")
    @Size(max = 255, message = "Location must not exceed 255 characters.")
    String location,

    String description
) {}
