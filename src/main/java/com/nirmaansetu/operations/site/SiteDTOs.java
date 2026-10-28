package com.nirmaansetu.operations.site;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

record CreateSiteRequest(
    UUID projectId,
    String projectTitle,
    @NotBlank String name,
    @NotBlank String address,
    @NotBlank String city,
    @NotBlank String state,
    double latitude,
    double longitude,
    Integer geofenceRadiusMeters,
    String managerName,
    String contactPhone
) {}

record SiteResponse(
    UUID id,
    UUID projectId,
    String projectTitle,
    String name,
    String address,
    String city,
    String state,
    double latitude,
    double longitude,
    int geofenceRadiusMeters,
    String managerName,
    String contactPhone,
    Instant createdAt
) {
    public static SiteResponse from(SiteEntity s) {
        return new SiteResponse(
            s.getId(), s.getProjectId(), s.getProjectTitle(), s.getName(), s.getAddress(),
            s.getCity(), s.getState(), s.getLatitude(), s.getLongitude(), s.getGeofenceRadiusMeters(),
            s.getManagerName(), s.getContactPhone(), s.getCreatedAt()
        );
    }
}
