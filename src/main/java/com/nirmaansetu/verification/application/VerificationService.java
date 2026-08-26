package com.nirmaansetu.verification.application;

import com.nirmaansetu.client.infrastructure.persistence.ClientProfileEntity;
import com.nirmaansetu.client.infrastructure.persistence.ClientProfileRepository;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorProfileEntity;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorProfileRepository;
import com.nirmaansetu.verification.api.VerificationResponse;
import com.nirmaansetu.verification.domain.SubjectType;
import com.nirmaansetu.verification.domain.VerificationException;
import com.nirmaansetu.verification.domain.VerificationStatus;
import com.nirmaansetu.verification.infrastructure.persistence.VerificationEntity;
import com.nirmaansetu.verification.infrastructure.persistence.VerificationRepository;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileEntity;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final ContractorProfileRepository contractorProfileRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final Clock clock;

    public VerificationService(VerificationRepository verificationRepository,
                               WorkerProfileRepository workerProfileRepository,
                               ContractorProfileRepository contractorProfileRepository,
                               ClientProfileRepository clientProfileRepository,
                               Clock clock) {
        this.verificationRepository = verificationRepository;
        this.workerProfileRepository = workerProfileRepository;
        this.contractorProfileRepository = contractorProfileRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.clock = clock;
    }

    @Transactional
    public VerificationResponse submitUserVerification(UUID userId) {
        return submitVerification(userId, SubjectType.USER);
    }

    @Transactional
    public VerificationResponse submitWorkerVerification(UUID userId) {
        return submitVerification(userId, SubjectType.WORKER);
    }

    @Transactional
    public VerificationResponse submitContractorVerification(UUID userId) {
        return submitVerification(userId, SubjectType.CONTRACTOR);
    }

    @Transactional
    public VerificationResponse submitClientVerification(UUID userId) {
        return submitVerification(userId, SubjectType.CLIENT);
    }

    private VerificationResponse submitVerification(UUID userId, SubjectType subjectType) {
        Instant now = clock.instant();
        UUID entityId = UUID.randomUUID();
        VerificationEntity entity = new VerificationEntity(entityId, subjectType, VerificationStatus.PENDING, now);

        switch (subjectType) {
            case USER -> {
                validateSubjectStatus(verificationRepository.findFirstBySubjectUserIdAndStatus(userId, VerificationStatus.PENDING),
                        verificationRepository.findFirstBySubjectUserIdAndStatus(userId, VerificationStatus.VERIFIED));
                entity.setSubjectUserId(userId);
            }
            case WORKER -> {
                WorkerProfileEntity workerProfile = workerProfileRepository.findByUserId(userId)
                        .orElseThrow(() -> new VerificationException("Worker profile not found for user"));
                validateSubjectStatus(verificationRepository.findFirstBySubjectWorkerProfileIdAndStatus(workerProfile.getId(), VerificationStatus.PENDING),
                        verificationRepository.findFirstBySubjectWorkerProfileIdAndStatus(workerProfile.getId(), VerificationStatus.VERIFIED));
                entity.setSubjectWorkerProfileId(workerProfile.getId());
            }
            case CONTRACTOR -> {
                ContractorProfileEntity contractorProfile = contractorProfileRepository.findByUserId(userId)
                        .orElseThrow(() -> new VerificationException("Contractor profile not found for user"));
                validateSubjectStatus(verificationRepository.findFirstBySubjectContractorProfileIdAndStatus(contractorProfile.getId(), VerificationStatus.PENDING),
                        verificationRepository.findFirstBySubjectContractorProfileIdAndStatus(contractorProfile.getId(), VerificationStatus.VERIFIED));
                entity.setSubjectContractorProfileId(contractorProfile.getId());
            }
            case CLIENT -> {
                ClientProfileEntity clientProfile = clientProfileRepository.findByUserId(userId)
                        .orElseThrow(() -> new VerificationException("Client profile not found for user"));
                validateSubjectStatus(verificationRepository.findFirstBySubjectClientProfileIdAndStatus(clientProfile.getId(), VerificationStatus.PENDING),
                        verificationRepository.findFirstBySubjectClientProfileIdAndStatus(clientProfile.getId(), VerificationStatus.VERIFIED));
                entity.setSubjectClientProfileId(clientProfile.getId());
            }
        }

        VerificationEntity saved = verificationRepository.save(entity);
        return VerificationResponse.fromEntity(saved);
    }

    private void validateSubjectStatus(Optional<VerificationEntity> pendingOpt, Optional<VerificationEntity> verifiedOpt) {
        if (pendingOpt.isPresent()) {
            throw new VerificationException("Verification request is already PENDING for this subject");
        }
        if (verifiedOpt.isPresent()) {
            throw new VerificationException("Subject is already VERIFIED");
        }
    }

    @Transactional(readOnly = true)
    public List<VerificationResponse> getMyVerifications(UUID userId) {
        List<VerificationEntity> all = new ArrayList<>();
        all.addAll(verificationRepository.findBySubjectUserIdOrderByCreatedAtDesc(userId));

        workerProfileRepository.findByUserId(userId)
                .ifPresent(w -> all.addAll(verificationRepository.findBySubjectWorkerProfileIdOrderByCreatedAtDesc(w.getId())));

        contractorProfileRepository.findByUserId(userId)
                .ifPresent(c -> all.addAll(verificationRepository.findBySubjectContractorProfileIdOrderByCreatedAtDesc(c.getId())));

        clientProfileRepository.findByUserId(userId)
                .ifPresent(cl -> all.addAll(verificationRepository.findBySubjectClientProfileIdOrderByCreatedAtDesc(cl.getId())));

        all.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return all.stream().map(VerificationResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<VerificationResponse> listVerificationsForAdmin(SubjectType subjectType, VerificationStatus status) {
        return verificationRepository.findAllFiltered(subjectType, status)
                .stream()
                .map(VerificationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public VerificationResponse getVerificationByIdForAdmin(UUID verificationId) {
        VerificationEntity entity = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new VerificationException("Verification not found: " + verificationId));
        return VerificationResponse.fromEntity(entity);
    }

    @Transactional
    public VerificationResponse verifyVerification(UUID adminUserId, UUID verificationId, String notes) {
        return reviewVerification(adminUserId, verificationId, VerificationStatus.VERIFIED, notes);
    }

    @Transactional
    public VerificationResponse rejectVerification(UUID adminUserId, UUID verificationId, String notes) {
        return reviewVerification(adminUserId, verificationId, VerificationStatus.REJECTED, notes);
    }

    private VerificationResponse reviewVerification(UUID adminUserId, UUID verificationId, VerificationStatus targetStatus, String notes) {
        VerificationEntity entity = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new VerificationException("Verification not found: " + verificationId));

        if (entity.getStatus() != VerificationStatus.PENDING) {
            throw new VerificationException("Only PENDING verification records can be reviewed. Current status: " + entity.getStatus());
        }

        Instant now = clock.instant();
        entity.setStatus(targetStatus);
        entity.setReviewedByUserId(adminUserId);
        entity.setReviewedAt(now);
        entity.setNotes(notes);
        entity.setUpdatedAt(now);

        VerificationEntity saved = verificationRepository.save(entity);
        return VerificationResponse.fromEntity(saved);
    }
}
