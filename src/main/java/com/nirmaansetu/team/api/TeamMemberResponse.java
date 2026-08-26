package com.nirmaansetu.team.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TeamMemberResponse(
    UUID teamId,
    UUID workerProfileId,
    LocalDate startsOn,
    LocalDate endsOn,
    Instant createdAt
) {}
