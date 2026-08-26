package com.nirmaansetu.contractor.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ContractorWorkerResponse(
    UUID contractorProfileId,
    UUID workerProfileId,
    LocalDate startsOn,
    LocalDate endsOn,
    Instant createdAt
) {}
