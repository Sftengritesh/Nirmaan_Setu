package com.nirmaansetu.contractor.application;

import com.nirmaansetu.contractor.api.AssociateWorkerRequest;
import com.nirmaansetu.contractor.api.ContractorProfileResponse;
import com.nirmaansetu.contractor.api.ContractorWorkerResponse;
import com.nirmaansetu.contractor.api.CreateContractorProfileRequest;
import com.nirmaansetu.contractor.api.UpdateContractorProfileRequest;
import com.nirmaansetu.contractor.domain.ContractorProfileException;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorProfileEntity;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorProfileRepository;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorWorkerAssociationEntity;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorWorkerAssociationId;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorWorkerAssociationRepository;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContractorService {

    private final ContractorProfileRepository contractorProfileRepository;
    private final ContractorWorkerAssociationRepository associationRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final Clock clock;

    public ContractorService(ContractorProfileRepository contractorProfileRepository,
                             ContractorWorkerAssociationRepository associationRepository,
                             WorkerProfileRepository workerProfileRepository,
                             Clock clock) {
        this.contractorProfileRepository = contractorProfileRepository;
        this.associationRepository = associationRepository;
        this.workerProfileRepository = workerProfileRepository;
        this.clock = clock;
    }

    @Transactional
    public ContractorProfileResponse createProfile(UUID userId, CreateContractorProfileRequest request) {
        if (contractorProfileRepository.existsByUserId(userId)) {
            throw new ContractorProfileException("A contractor profile already exists for this account.");
        }
        Instant now = clock.instant();
        ContractorProfileEntity entity = new ContractorProfileEntity(
            UUID.randomUUID(), userId, request.displayName().trim(), request.location().trim(), now
        );
        entity.setDescription(request.description());
        try {
            entity = contractorProfileRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new ContractorProfileException("A contractor profile already exists for this account.");
        }
        return toProfileResponse(entity);
    }

    @Transactional(readOnly = true)
    public ContractorProfileResponse getProfile(UUID userId) {
        ContractorProfileEntity entity = contractorProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ContractorProfileException("Contractor profile not found."));
        return toProfileResponse(entity);
    }

    @Transactional
    public ContractorProfileResponse updateProfile(UUID userId, UpdateContractorProfileRequest request) {
        ContractorProfileEntity entity = contractorProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ContractorProfileException("Contractor profile not found."));
        entity.setDisplayName(request.displayName().trim());
        entity.setLocation(request.location().trim());
        entity.setDescription(request.description());
        entity.setUpdatedAt(clock.instant());
        return toProfileResponse(contractorProfileRepository.save(entity));
    }

    @Transactional
    public ContractorWorkerResponse associateWorker(UUID userId, AssociateWorkerRequest request) {
        ContractorProfileEntity profile = contractorProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ContractorProfileException("Contractor profile not found."));
        
        if (!workerProfileRepository.existsById(request.workerProfileId())) {
            throw new ContractorProfileException("Worker profile not found.");
        }

        if (request.endsOn() != null && request.endsOn().isBefore(request.startsOn())) {
            throw new ContractorProfileException("End date cannot be before start date.");
        }

        ContractorWorkerAssociationId id = new ContractorWorkerAssociationId(
            profile.getId(), request.workerProfileId(), request.startsOn()
        );
        Instant now = clock.instant();
        ContractorWorkerAssociationEntity entity = new ContractorWorkerAssociationEntity(
            id, request.endsOn(), now
        );
        try {
            entity = associationRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new ContractorProfileException("A worker association with this start date already exists.");
        }
        return toAssociationResponse(entity);
    }

    @Transactional
    public ContractorWorkerResponse endWorkerAssociation(UUID userId, UUID workerProfileId, LocalDate startsOn, LocalDate endsOn) {
        ContractorProfileEntity profile = contractorProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ContractorProfileException("Contractor profile not found."));

        ContractorWorkerAssociationId id = new ContractorWorkerAssociationId(
            profile.getId(), workerProfileId, startsOn
        );

        ContractorWorkerAssociationEntity entity = associationRepository.findById(id)
            .orElseThrow(() -> new ContractorProfileException("Contractor worker association not found."));

        if (endsOn != null && endsOn.isBefore(id.getStartsOn())) {
            throw new ContractorProfileException("End date cannot be before start date.");
        }

        entity.setEndsOn(endsOn);
        return toAssociationResponse(associationRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<ContractorWorkerResponse> getAssociatedWorkers(UUID userId) {
        ContractorProfileEntity profile = contractorProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ContractorProfileException("Contractor profile not found."));

        return associationRepository.findByIdContractorProfileId(profile.getId()).stream()
            .map(this::toAssociationResponse)
            .toList();
    }

    private ContractorProfileResponse toProfileResponse(ContractorProfileEntity entity) {
        return new ContractorProfileResponse(
            entity.getId(),
            entity.getUserId(),
            entity.getDisplayName(),
            entity.getDescription(),
            entity.getLocation(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private ContractorWorkerResponse toAssociationResponse(ContractorWorkerAssociationEntity entity) {
        return new ContractorWorkerResponse(
            entity.getId().getContractorProfileId(),
            entity.getId().getWorkerProfileId(),
            entity.getId().getStartsOn(),
            entity.getEndsOn(),
            entity.getCreatedAt()
        );
    }
}
