package com.nirmaansetu.verification.api;

import com.nirmaansetu.verification.infrastructure.persistence.VerificationEntity;
import java.time.Instant;
import java.util.UUID;

public record VerificationResponse(
    UUID id,
    String subjectType,
    UUID subjectUserId,
    UUID subjectWorkerProfileId,
    UUID subjectContractorProfileId,
    UUID subjectClientProfileId,
    String verificationType,
    String status,
    UUID reviewedByUserId,
    Instant reviewedAt,
    String notes,
    Instant createdAt,
    Instant updatedAt
) {
    public static VerificationResponse fromEntity(VerificationEntity entity) {
        return new VerificationResponse(
            entity.getId(),
            entity.getSubjectType().name(),
            entity.getSubjectUserId(),
            entity.getSubjectWorkerProfileId(),
            entity.getSubjectContractorProfileId(),
            entity.getSubjectClientProfileId(),
            entity.getVerificationType(),
            entity.getStatus().name(),
            entity.getReviewedByUserId(),
            entity.getReviewedAt(),
            entity.getNotes(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
