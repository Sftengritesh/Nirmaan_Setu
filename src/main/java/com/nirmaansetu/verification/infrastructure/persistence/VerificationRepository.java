package com.nirmaansetu.verification.infrastructure.persistence;

import com.nirmaansetu.verification.domain.SubjectType;
import com.nirmaansetu.verification.domain.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificationRepository extends JpaRepository<VerificationEntity, UUID> {

    List<VerificationEntity> findBySubjectUserIdOrderByCreatedAtDesc(UUID subjectUserId);

    List<VerificationEntity> findBySubjectWorkerProfileIdOrderByCreatedAtDesc(UUID subjectWorkerProfileId);

    List<VerificationEntity> findBySubjectContractorProfileIdOrderByCreatedAtDesc(UUID subjectContractorProfileId);

    List<VerificationEntity> findBySubjectClientProfileIdOrderByCreatedAtDesc(UUID subjectClientProfileId);

    Optional<VerificationEntity> findFirstBySubjectUserIdAndStatus(UUID subjectUserId, VerificationStatus status);

    Optional<VerificationEntity> findFirstBySubjectWorkerProfileIdAndStatus(UUID subjectWorkerProfileId, VerificationStatus status);

    Optional<VerificationEntity> findFirstBySubjectContractorProfileIdAndStatus(UUID subjectContractorProfileId, VerificationStatus status);

    Optional<VerificationEntity> findFirstBySubjectClientProfileIdAndStatus(UUID subjectClientProfileId, VerificationStatus status);

    @Query("SELECT v FROM VerificationEntity v WHERE (:subjectType IS NULL OR v.subjectType = :subjectType) AND (:status IS NULL OR v.status = :status) ORDER BY v.createdAt DESC")
    List<VerificationEntity> findAllFiltered(@Param("subjectType") SubjectType subjectType, @Param("status") VerificationStatus status);
}
