package com.nirmaansetu.team.api;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record AddTeamMemberRequest(
    @NotNull(message = "Worker profile ID is required.")
    UUID workerProfileId,

    @NotNull(message = "Starts on date is required.")
    LocalDate startsOn,

    LocalDate endsOn
) {}
