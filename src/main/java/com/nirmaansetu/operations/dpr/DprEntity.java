package com.nirmaansetu.operations.dpr;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_progress_report")
public class DprEntity {

    @Id
    private UUID id;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "project_title", length = 200)
    private String projectTitle;

    @Column(name = "site_id")
    private UUID siteId;

    @Column(name = "site_name", length = 160)
    private String siteName;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "reported_by", nullable = false, length = 120)
    private String reportedBy;

    @Column(name = "reported_by_user_id")
    private UUID reportedByUserId;

    @Column(name = "weather_condition", nullable = false, length = 40)
    private String weatherCondition;

    @Column(name = "skilled_labor_count", nullable = false)
    private int skilledLaborCount;

    @Column(name = "unskilled_labor_count", nullable = false)
    private int unskilledLaborCount;

    @Column(name = "tasks_completed", nullable = false, columnDefinition = "TEXT")
    private String tasksCompleted;

    @Column(name = "delays_or_issues", columnDefinition = "TEXT")
    private String delaysOrIssues;

    @Column(name = "safety_observations", columnDefinition = "TEXT")
    private String safetyObservations;

    @Column(columnDefinition = "TEXT")
    private String photos;

    @Column(nullable = false, length = 20)
    private String status = "SUBMITTED";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public DprEntity() {}

    public DprEntity(UUID id, UUID projectId, String projectTitle, UUID siteId, String siteName,
                     LocalDate reportDate, String reportedBy, UUID reportedByUserId,
                     String weatherCondition, int skilledLaborCount, int unskilledLaborCount,
                     String tasksCompleted, String delaysOrIssues, String photos) {
        this.id = id;
        this.projectId = projectId;
        this.projectTitle = projectTitle;
        this.siteId = siteId;
        this.siteName = siteName;
        this.reportDate = reportDate;
        this.reportedBy = reportedBy;
        this.reportedByUserId = reportedByUserId;
        this.weatherCondition = weatherCondition;
        this.skilledLaborCount = skilledLaborCount;
        this.unskilledLaborCount = unskilledLaborCount;
        this.tasksCompleted = tasksCompleted;
        this.delaysOrIssues = delaysOrIssues;
        this.photos = photos;
        this.status = "SUBMITTED";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }
    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }
    public UUID getSiteId() { return siteId; }
    public void setSiteId(UUID siteId) { this.siteId = siteId; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    public UUID getReportedByUserId() { return reportedByUserId; }
    public void setReportedByUserId(UUID reportedByUserId) { this.reportedByUserId = reportedByUserId; }
    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }
    public int getSkilledLaborCount() { return skilledLaborCount; }
    public void setSkilledLaborCount(int skilledLaborCount) { this.skilledLaborCount = skilledLaborCount; }
    public int getUnskilledLaborCount() { return unskilledLaborCount; }
    public void setUnskilledLaborCount(int unskilledLaborCount) { this.unskilledLaborCount = unskilledLaborCount; }
    public String getTasksCompleted() { return tasksCompleted; }
    public void setTasksCompleted(String tasksCompleted) { this.tasksCompleted = tasksCompleted; }
    public String getDelaysOrIssues() { return delaysOrIssues; }
    public void setDelaysOrIssues(String delaysOrIssues) { this.delaysOrIssues = delaysOrIssues; }
    public String getSafetyObservations() { return safetyObservations; }
    public void setSafetyObservations(String safetyObservations) { this.safetyObservations = safetyObservations; }
    public String getPhotos() { return photos; }
    public void setPhotos(String photos) { this.photos = photos; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
