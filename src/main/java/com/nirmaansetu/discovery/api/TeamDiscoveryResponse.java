package com.nirmaansetu.discovery.api;

import java.util.UUID;

public record TeamDiscoveryResponse(
    UUID id,
    UUID managerUserId,
    String name,
    String description,
    String status,
    Long activeMemberCount
) {}
