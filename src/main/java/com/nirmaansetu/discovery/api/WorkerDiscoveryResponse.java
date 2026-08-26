package com.nirmaansetu.discovery.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record WorkerDiscoveryResponse(
    UUID id,
    String displayName,
    Short experienceYears,
    String location,
    String availabilityStatus,
    BigDecimal dailyRate,
    String profileDescription,
    Boolean isTravelWilling,
    List<UUID> skillIds,
    Boolean isVerified
) {}
