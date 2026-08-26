package com.nirmaansetu.client.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateClientProfileRequest(
    @NotBlank(message = "Client type is required")
    String clientType,

    @NotBlank(message = "Display name is required")
    @Size(max = 160, message = "Display name must not exceed 160 characters")
    String displayName
) {}
