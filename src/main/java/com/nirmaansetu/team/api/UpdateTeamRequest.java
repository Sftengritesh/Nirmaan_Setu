package com.nirmaansetu.team.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTeamRequest(
    @NotBlank(message = "Team name is required.")
    @Size(max = 160, message = "Team name must not exceed 160 characters.")
    String name,

    String description,

    String status
) {}
