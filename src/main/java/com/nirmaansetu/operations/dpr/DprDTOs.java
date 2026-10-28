package com.nirmaansetu.operations.dpr;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

record CreateDprRequest(
    UUID projectId,
    String projectTitle,
    UUID siteId,
    String siteName,
    @NotNull LocalDate reportDate,
    String reportedBy,
    @NotBlank String weatherCondition,
    int skilledLaborCount,
    int unskilledLaborCount,
    @NotBlank String tasksCompleted,
    String delaysOrIssues,
    String safetyObservations,
    String photos
) {}

record DprResponse(
    UUID id,
    UUID projectId,
    String projectTitle,
    UUID siteId,
    String siteName,
    LocalDate reportDate,
    String reportedBy,
    UUID reportedByUserId,
    String weatherCondition,
    int skilledLaborCount,
    int unskilledLaborCount,
    String tasksCompleted,
    String delaysOrIssues,
    String safetyObservations,
    String photos,
    String status,
    Instant createdAt
) {
    public static DprResponse from(DprEntity d) {
        return new DprResponse(
            d.getId(), d.getProjectId(), d.getProjectTitle(), d.getSiteId(), d.getSiteName(),
            d.getReportDate(), d.getReportedBy(), d.getReportedByUserId(),
            d.getWeatherCondition(), d.getSkilledLaborCount(), d.getUnskilledLaborCount(),
            d.getTasksCompleted(), d.getDelaysOrIssues(), d.getSafetyObservations(),
            d.getPhotos(), d.getStatus(), d.getCreatedAt()
        );
    }
}
