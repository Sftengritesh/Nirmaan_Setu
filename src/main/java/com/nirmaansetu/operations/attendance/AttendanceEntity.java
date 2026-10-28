package com.nirmaansetu.operations.attendance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "attendance")
public class AttendanceEntity {

    @Id
    private UUID id;

    @Column(name = "worker_user_id", nullable = false)
    private UUID workerUserId;

    @Column(name = "worker_name", nullable = false, length = 120)
    private String workerName;

    @Column(name = "worker_trade", length = 80)
    private String workerTrade;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "project_title", length = 200)
    private String projectTitle;

    @Column(name = "site_id")
    private UUID siteId;

    @Column(name = "site_name", length = 160)
    private String siteName;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "check_in_time", nullable = false, length = 30)
    private String checkInTime;

    @Column(name = "check_out_time", length = 30)
    private String checkOutTime;

    @Column(nullable = false, length = 20)
    private String status = "PRESENT";

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "is_within_geofence", nullable = false)
    private boolean isWithinGeofence = true;

    @Column(name = "distance_from_site_meters", nullable = false)
    private int distanceFromSiteMeters = 0;

    @Column(name = "selfie_url", columnDefinition = "TEXT")
    private String selfieUrl;

    @Column(name = "approval_status", nullable = false, length = 20)
    private String approvalStatus = "PENDING";

    @Column(name = "approved_by", length = 120)
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "hours_worked", precision = 5, scale = 2)
    private BigDecimal hoursWorked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public AttendanceEntity() {}

    public AttendanceEntity(UUID id, UUID workerUserId, String workerName, String workerTrade,
                            UUID projectId, String projectTitle, UUID siteId, String siteName,
                            LocalDate workDate, String checkInTime, Double latitude, Double longitude,
                            boolean isWithinGeofence, int distanceFromSiteMeters, String selfieUrl) {
        this.id = id;
        this.workerUserId = workerUserId;
        this.workerName = workerName;
        this.workerTrade = workerTrade;
        this.projectId = projectId;
        this.projectTitle = projectTitle;
        this.siteId = siteId;
        this.siteName = siteName;
        this.workDate = workDate;
        this.checkInTime = checkInTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isWithinGeofence = isWithinGeofence;
        this.distanceFromSiteMeters = distanceFromSiteMeters;
        this.selfieUrl = selfieUrl;
        this.status = "PRESENT";
        this.approvalStatus = "PENDING";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getWorkerUserId() { return workerUserId; }
    public void setWorkerUserId(UUID workerUserId) { this.workerUserId = workerUserId; }
    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }
    public String getWorkerTrade() { return workerTrade; }
    public void setWorkerTrade(String workerTrade) { this.workerTrade = workerTrade; }
    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }
    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }
    public UUID getSiteId() { return siteId; }
    public void setSiteId(UUID siteId) { this.siteId = siteId; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }
    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }
    public String getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public boolean isWithinGeofence() { return isWithinGeofence; }
    public void setWithinGeofence(boolean withinGeofence) { isWithinGeofence = withinGeofence; }
    public int getDistanceFromSiteMeters() { return distanceFromSiteMeters; }
    public void setDistanceFromSiteMeters(int distanceFromSiteMeters) { this.distanceFromSiteMeters = distanceFromSiteMeters; }
    public String getSelfieUrl() { return selfieUrl; }
    public void setSelfieUrl(String selfieUrl) { this.selfieUrl = selfieUrl; }
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public BigDecimal getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(BigDecimal hoursWorked) { this.hoursWorked = hoursWorked; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
