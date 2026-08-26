package com.nirmaansetu.worker.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import com.nirmaansetu.worker.domain.AvailabilityStatus;

@Entity
@Table(name = "worker_profile")
public class WorkerProfileEntity {

    @Id private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "experience_years")
    private Short experienceYears;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "availability_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availabilityStatus;

    @Column(name = "daily_rate")
    private BigDecimal dailyRate;

    @Column(name = "profile_description")
    private String profileDescription;

    @Column(name = "is_travel_willing", nullable = false)
    private boolean isTravelWilling;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "worker_skill",
        joinColumns = @JoinColumn(name = "worker_profile_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<SkillEntity> skills = new LinkedHashSet<>();

    protected WorkerProfileEntity() {}

    public WorkerProfileEntity(UUID id, UUID userId, String displayName, String location,
                               AvailabilityStatus availabilityStatus, boolean isTravelWilling,
                               Instant now) {
        this.id = id;
        this.userId = userId;
        this.displayName = displayName;
        this.location = location;
        this.availabilityStatus = availabilityStatus;
        this.isTravelWilling = isTravelWilling;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Short getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Short experienceYears) { this.experienceYears = experienceYears; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public AvailabilityStatus getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) { this.availabilityStatus = availabilityStatus; }
    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }
    public String getProfileDescription() { return profileDescription; }
    public void setProfileDescription(String profileDescription) { this.profileDescription = profileDescription; }
    public boolean isTravelWilling() { return isTravelWilling; }
    public void setTravelWilling(boolean travelWilling) { this.isTravelWilling = travelWilling; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Set<SkillEntity> getSkills() { return skills; }

    public boolean addSkill(SkillEntity skill) { return skills.add(skill); }
    public boolean removeSkill(UUID skillId) { return skills.removeIf(s -> s.getId().equals(skillId)); }
}
