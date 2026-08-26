package com.nirmaansetu.requirement.infrastructure.persistence;

import com.nirmaansetu.requirement.domain.RequirementStatus;
import com.nirmaansetu.requirement.domain.WorkerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "workforce_requirement")
public class WorkforceRequirementEntity {

    @Id
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "worker_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkerType workerType;

    @Column(name = "skill_id", nullable = false)
    private UUID skillId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "daily_rate")
    private BigDecimal dailyRate;

    @Column(name = "budget_amount")
    private BigDecimal budgetAmount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "accommodation_available", nullable = false)
    private Boolean accommodationAvailable;

    @Column(name = "food_available", nullable = false)
    private Boolean foodAvailable;

    @Column(name = "additional_notes")
    private String additionalNotes;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RequirementStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected WorkforceRequirementEntity() {}

    public WorkforceRequirementEntity(UUID id, UUID projectId, String location, LocalDate startDate,
                                      Integer durationDays, WorkerType workerType, UUID skillId,
                                      Integer quantity, String currencyCode, Boolean accommodationAvailable,
                                      Boolean foodAvailable, RequirementStatus status, Instant now) {
        this.id = id;
        this.projectId = projectId;
        this.location = location;
        this.startDate = startDate;
        this.durationDays = durationDays;
        this.workerType = workerType;
        this.skillId = skillId;
        this.quantity = quantity;
        this.currencyCode = currencyCode != null ? currencyCode : "INR";
        this.accommodationAvailable = accommodationAvailable != null ? accommodationAvailable : false;
        this.foodAvailable = foodAvailable != null ? foodAvailable : false;
        this.status = status;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getProjectId() { return projectId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public WorkerType getWorkerType() { return workerType; }
    public void setWorkerType(WorkerType workerType) { this.workerType = workerType; }
    public UUID getSkillId() { return skillId; }
    public void setSkillId(UUID skillId) { this.skillId = skillId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }
    public BigDecimal getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public Boolean getAccommodationAvailable() { return accommodationAvailable; }
    public void setAccommodationAvailable(Boolean accommodationAvailable) { this.accommodationAvailable = accommodationAvailable; }
    public Boolean getFoodAvailable() { return foodAvailable; }
    public void setFoodAvailable(Boolean foodAvailable) { this.foodAvailable = foodAvailable; }
    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
    public RequirementStatus getStatus() { return status; }
    public void setStatus(RequirementStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
