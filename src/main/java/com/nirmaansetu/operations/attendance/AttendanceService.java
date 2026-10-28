package com.nirmaansetu.operations.attendance;

import com.nirmaansetu.operations.site.SiteEntity;
import com.nirmaansetu.operations.site.SiteRepository;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileEntity;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SiteRepository siteRepository;
    private final WorkerProfileRepository workerProfileRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             SiteRepository siteRepository,
                             WorkerProfileRepository workerProfileRepository) {
        this.attendanceRepository = attendanceRepository;
        this.siteRepository = siteRepository;
        this.workerProfileRepository = workerProfileRepository;
    }

    @Transactional
    public AttendanceResponse checkIn(UUID workerUserId, CheckInRequest req) {
        LocalDate today = LocalDate.now();

        // 1. Prevent duplicate check-in on the same date
        Optional<AttendanceEntity> existing = attendanceRepository.findByWorkerUserIdAndWorkDate(workerUserId, today);
        if (existing.isPresent()) {
            throw new IllegalStateException("Already checked in for today (" + today + ")");
        }

        // 2. Fetch worker display name and trade
        Optional<WorkerProfileEntity> workerOpt = workerProfileRepository.findByUserId(workerUserId);
        String workerName = workerOpt.map(WorkerProfileEntity::getDisplayName).orElse("Worker");
        String trade = "Skilled Tradesperson";

        // 3. Validate geofence against target site
        SiteEntity site = siteRepository.findById(req.siteId())
            .orElseThrow(() -> new IllegalArgumentException("Site not found: " + req.siteId()));

        int distanceMeters = calculateHaversineDistance(
            req.latitude(), req.longitude(), site.getLatitude(), site.getLongitude()
        );
        boolean withinGeofence = distanceMeters <= site.getGeofenceRadiusMeters();

        String checkInTimeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));

        AttendanceEntity entity = new AttendanceEntity(
            UUID.randomUUID(),
            workerUserId,
            workerName,
            trade,
            req.projectId() != null ? req.projectId() : site.getProjectId(),
            req.projectTitle() != null ? req.projectTitle() : site.getProjectTitle(),
            site.getId(),
            site.getName(),
            today,
            checkInTimeStr,
            req.latitude(),
            req.longitude(),
            withinGeofence,
            distanceMeters,
            req.selfieUrl()
        );

        if (req.remarks() != null) {
            entity.setRemarks(req.remarks());
        }

        return AttendanceResponse.from(attendanceRepository.save(entity));
    }

    @Transactional
    public AttendanceResponse checkOut(UUID workerUserId, CheckOutRequest req) {
        LocalDate today = LocalDate.now();
        AttendanceEntity entity = attendanceRepository.findByWorkerUserIdAndWorkDate(workerUserId, today)
            .orElseThrow(() -> new IllegalStateException("No active check-in found for today. Please check in first."));

        if (entity.getCheckOutTime() != null) {
            throw new IllegalStateException("Already checked out for today at " + entity.getCheckOutTime());
        }

        LocalTime now = LocalTime.now();
        String checkOutTimeStr = now.format(DateTimeFormatter.ofPattern("hh:mm a"));
        entity.setCheckOutTime(checkOutTimeStr);

        // Approximate duration
        try {
            LocalTime checkInTime = LocalTime.parse(entity.getCheckInTime(), DateTimeFormatter.ofPattern("hh:mm a"));
            Duration duration = Duration.between(checkInTime, now);
            if (duration.isNegative()) {
                duration = duration.plusHours(24);
            }
            double hours = duration.toMinutes() / 60.0;
            entity.setHoursWorked(BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP));
        } catch (Exception ignored) {
            entity.setHoursWorked(BigDecimal.valueOf(8.0));
        }

        if (req != null && req.remarks() != null && !req.remarks().isBlank()) {
            String updatedRemarks = entity.getRemarks() != null ? entity.getRemarks() + " | Check-out: " + req.remarks() : req.remarks();
            entity.setRemarks(updatedRemarks);
        }

        entity.setUpdatedAt(Instant.now());
        return AttendanceResponse.from(attendanceRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getMyAttendance(UUID workerUserId) {
        return attendanceRepository.findByWorkerUserIdOrderByWorkDateDesc(workerUserId)
            .stream().map(AttendanceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAllAttendance(LocalDate date) {
        LocalDate queryDate = date != null ? date : LocalDate.now();
        return attendanceRepository.findByWorkDateOrderByCreatedAtDesc(queryDate)
            .stream().map(AttendanceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getSiteAttendance(UUID siteId) {
        return attendanceRepository.findBySiteIdOrderByWorkDateDesc(siteId)
            .stream().map(AttendanceResponse::from).toList();
    }

    @Transactional
    public AttendanceResponse reviewAttendance(UUID attendanceId, String approvalStatus, String approverName, String remarks) {
        AttendanceEntity entity = attendanceRepository.findById(attendanceId)
            .orElseThrow(() -> new IllegalArgumentException("Attendance record not found: " + attendanceId));

        entity.setApprovalStatus(approvalStatus.toUpperCase().trim());
        entity.setApprovedBy(approverName);
        entity.setApprovedAt(Instant.now());
        if (remarks != null && !remarks.isBlank()) {
            entity.setRemarks(remarks);
        }
        entity.setUpdatedAt(Instant.now());

        return AttendanceResponse.from(attendanceRepository.save(entity));
    }

    /**
     * Haversine formula to compute great-circle distance between two GPS coordinates in meters.
     */
    private int calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth radius in meters
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) Math.round(R * c);
    }
}
