package com.nirmaansetu.worker.application;

import com.nirmaansetu.worker.api.CreateWorkerProfileRequest;
import com.nirmaansetu.worker.api.UpdateWorkerProfileRequest;
import com.nirmaansetu.worker.api.WorkerProfileResponse;
import com.nirmaansetu.worker.api.WorkerProfileResponse.SkillResponse;
import com.nirmaansetu.worker.domain.AvailabilityStatus;
import com.nirmaansetu.worker.domain.WorkerProfileException;
import com.nirmaansetu.worker.infrastructure.persistence.SkillEntity;
import com.nirmaansetu.worker.infrastructure.persistence.SkillRepository;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileEntity;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkerService {

    private final WorkerProfileRepository workerProfileRepository;
    private final SkillRepository skillRepository;
    private final Clock clock;

    public WorkerService(WorkerProfileRepository workerProfileRepository,
                         SkillRepository skillRepository,
                         Clock clock) {
        this.workerProfileRepository = workerProfileRepository;
        this.skillRepository = skillRepository;
        this.clock = clock;
    }

    @Transactional
    public WorkerProfileResponse createProfile(UUID userId, CreateWorkerProfileRequest request) {
        if (workerProfileRepository.existsByUserId(userId)) {
            throw new WorkerProfileException("A worker profile already exists for this account.");
        }
        AvailabilityStatus status = parseAvailability(request.availabilityStatus());
        Instant now = clock.instant();
        WorkerProfileEntity entity = new WorkerProfileEntity(
            UUID.randomUUID(), userId, request.displayName().trim(),
            request.location().trim(), status, request.isTravelWilling(), now
        );
        entity.setExperienceYears(request.experienceYears());
        entity.setDailyRate(request.dailyRate());
        entity.setProfileDescription(request.profileDescription());
        try {
            entity = workerProfileRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new WorkerProfileException("A worker profile already exists for this account.");
        }
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public WorkerProfileResponse getProfile(UUID userId) {
        WorkerProfileEntity entity = workerProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new WorkerProfileException("Worker profile not found."));
        return toResponse(entity);
    }

    @Transactional
    public WorkerProfileResponse updateProfile(UUID userId, UpdateWorkerProfileRequest request) {
        WorkerProfileEntity entity = workerProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new WorkerProfileException("Worker profile not found."));
        AvailabilityStatus status = parseAvailability(request.availabilityStatus());
        entity.setDisplayName(request.displayName().trim());
        entity.setLocation(request.location().trim());
        entity.setAvailabilityStatus(status);
        entity.setExperienceYears(request.experienceYears());
        entity.setDailyRate(request.dailyRate());
        entity.setProfileDescription(request.profileDescription());
        entity.setTravelWilling(request.isTravelWilling());
        return toResponse(workerProfileRepository.save(entity));
    }

    @Transactional
    public WorkerProfileResponse addSkill(UUID userId, UUID skillId) {
        WorkerProfileEntity entity = workerProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new WorkerProfileException("Worker profile not found."));
        SkillEntity skill = skillRepository.findByIdAndIsActiveTrue(skillId)
            .orElseThrow(() -> new WorkerProfileException("Skill not found or inactive."));
        boolean added = entity.addSkill(skill);
        if (!added) {
            throw new WorkerProfileException("Skill is already associated with this profile.");
        }
        return toResponse(workerProfileRepository.save(entity));
    }

    @Transactional
    public WorkerProfileResponse removeSkill(UUID userId, UUID skillId) {
        WorkerProfileEntity entity = workerProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new WorkerProfileException("Worker profile not found."));
        boolean removed = entity.removeSkill(skillId);
        if (!removed) {
            throw new WorkerProfileException("Skill is not associated with this profile.");
        }
        return toResponse(workerProfileRepository.save(entity));
    }

    private AvailabilityStatus parseAvailability(String raw) {
        try {
            return AvailabilityStatus.valueOf(raw);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new WorkerProfileException("Invalid availability status. Allowed values: AVAILABLE, LIMITED, UNAVAILABLE.");
        }
    }

    private WorkerProfileResponse toResponse(WorkerProfileEntity entity) {
        List<SkillResponse> skillResponses = entity.getSkills().stream()
            .map(s -> new SkillResponse(s.getId(), s.getCode(), s.getName()))
            .toList();
        return new WorkerProfileResponse(
            entity.getId(),
            entity.getUserId(),
            entity.getDisplayName(),
            entity.getExperienceYears(),
            entity.getLocation(),
            entity.getAvailabilityStatus().name(),
            entity.getDailyRate(),
            entity.getProfileDescription(),
            entity.isTravelWilling(),
            skillResponses,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
