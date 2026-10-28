package com.nirmaansetu.operations.attendance;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

record CheckInRequest(
    UUID projectId,
    String projectTitle,
    @NotNull UUID siteId,
    String siteName,
    @NotNull Double latitude,
    @NotNull Double longitude,
    String selfieUrl,
    String remarks
) {}

record CheckOutRequest(
    String remarks
) {}

record ReviewAttendanceRequest(
    @NotNull String approvalStatus,
    String remarks
) {}

record AttendanceResponse(
    UUID id,
    UUID workerUserId,
    String workerName,
    String workerTrade,
    UUID projectId,
    String projectTitle,
    UUID siteId,
    String siteName,
    LocalDate date,
    String checkInTime,
    String checkOutTime,
    String status,
    Double latitude,
    Double longitude,
    boolean isWithinGeofence,
    int distanceFromSiteMeters,
    String selfieUrl,
    String approvalStatus,
    String approvedBy,
    Instant approvedAt,
    String remarks,
    BigDecimal hoursWorked,
    Instant createdAt
) {
    public static AttendanceResponse from(AttendanceEntity a) {
        return new AttendanceResponse(
            a.getId(), a.getWorkerUserId(), a.getWorkerName(), a.getWorkerTrade(),
            a.getProjectId(), a.getProjectTitle(), a.getSiteId(), a.getSiteName(),
            a.getWorkDate(), a.getCheckInTime(), a.getCheckOutTime(), a.getStatus(),
            a.getLatitude(), a.getLongitude(), a.isWithinGeofence(), a.getDistanceFromSiteMeters(),
            a.getSelfieUrl(), a.getApprovalStatus(), a.getApprovedBy(), a.getApprovedAt(),
            a.getRemarks(), a.getHoursWorked(), a.getCreatedAt()
        );
    }
}
