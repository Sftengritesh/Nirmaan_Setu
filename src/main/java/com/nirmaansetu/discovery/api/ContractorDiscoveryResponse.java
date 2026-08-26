package com.nirmaansetu.discovery.api;

import java.util.UUID;

public record ContractorDiscoveryResponse(
    UUID id,
    String displayName,
    String description,
    String location,
    Boolean isVerified
) {}
