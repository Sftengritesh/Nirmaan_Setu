package com.nirmaansetu.auth.api;

import jakarta.validation.constraints.NotBlank;

public record OnboardUserRequest(
    @NotBlank String role,
    String displayName,
    String location
) {}
