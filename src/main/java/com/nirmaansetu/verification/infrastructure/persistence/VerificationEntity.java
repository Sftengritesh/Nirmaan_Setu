package com.nirmaansetu.verification.infrastructure.persistence;

import com.nirmaansetu.verification.domain.SubjectType;
import com.nirmaansetu.verification.domain.VerificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verification")
public class VerificationEntity {

    @Id
    private UUID id;

    @Column(name = "subject_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SubjectType subjectType;

    @Column(name = "subject_user_id")
    private UUID subjectUserId;

    @Column(name = "subject_worker_profile_id")
    private UUID subjectWorkerProfileId;

    @Column(name = "subject_contractor_profile_id")
    private UUID subjectContractorProfileId;

    @Column(name = "subject_client_profile_id")
    private UUID subjectClientProfileId;

    @Column(name = "verification_type", nullable = false)
    private String verificationType;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private VerificationStatus status;

    @Column(name = "reviewed_by_user_id")
    private UUID reviewedByUserId;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected VerificationEntity() {}

    public VerificationEntity(UUID id, SubjectType subjectType, VerificationStatus status, Instant now) {
        this.id = id;
        this.subjectType = subjectType;
        this.verificationType = "MANUAL";
        this.status = status;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public SubjectType getSubjectType() { return subjectType; }
    public UUID getSubjectUserId() { return subjectUserId; }
    public void setSubjectUserId(UUID subjectUserId) { this.subjectUserId = subjectUserId; }
    public UUID getSubjectWorkerProfileId() { return subjectWorkerProfileId; }
    public void setSubjectWorkerProfileId(UUID subjectWorkerProfileId) { this.subjectWorkerProfileId = subjectWorkerProfileId; }
    public UUID getSubjectContractorProfileId() { return subjectContractorProfileId; }
    public void setSubjectContractorProfileId(UUID subjectContractorProfileId) { this.subjectContractorProfileId = subjectContractorProfileId; }
    public UUID getSubjectClientProfileId() { return subjectClientProfileId; }
    public void setSubjectClientProfileId(UUID subjectClientProfileId) { this.subjectClientProfileId = subjectClientProfileId; }
    public String getVerificationType() { return verificationType; }
    public VerificationStatus getStatus() { return status; }
    public void setStatus(VerificationStatus status) { this.status = status; }
    public UUID getReviewedByUserId() { return reviewedByUserId; }
    public void setReviewedByUserId(UUID reviewedByUserId) { this.reviewedByUserId = reviewedByUserId; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
