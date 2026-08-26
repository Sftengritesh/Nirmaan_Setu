package com.nirmaansetu.requirement.application;

import com.nirmaansetu.client.infrastructure.persistence.ClientProfileEntity;
import com.nirmaansetu.client.infrastructure.persistence.ClientProfileRepository;
import com.nirmaansetu.project.infrastructure.persistence.ProjectEntity;
import com.nirmaansetu.project.infrastructure.persistence.ProjectRepository;
import com.nirmaansetu.requirement.api.CreateRequirementRequest;
import com.nirmaansetu.requirement.api.UpdateRequirementRequest;
import com.nirmaansetu.requirement.api.WorkforceRequirementResponse;
import com.nirmaansetu.requirement.domain.RequirementStatus;
import com.nirmaansetu.requirement.domain.WorkerType;
import com.nirmaansetu.requirement.domain.WorkforceRequirementException;
import com.nirmaansetu.requirement.infrastructure.persistence.WorkforceRequirementEntity;
import com.nirmaansetu.requirement.infrastructure.persistence.WorkforceRequirementRepository;
import com.nirmaansetu.worker.infrastructure.persistence.SkillRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkforceRequirementService {

    private final WorkforceRequirementRepository requirementRepository;
    private final ProjectRepository projectRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final SkillRepository skillRepository;
    private final Clock clock;

    public WorkforceRequirementService(WorkforceRequirementRepository requirementRepository,
                                       ProjectRepository projectRepository,
                                       ClientProfileRepository clientProfileRepository,
                                       SkillRepository skillRepository,
                                       Clock clock) {
        this.requirementRepository = requirementRepository;
        this.projectRepository = projectRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.skillRepository = skillRepository;
        this.clock = clock;
    }

    @Transactional
    public WorkforceRequirementResponse createRequirement(UUID userId, UUID projectId, CreateRequirementRequest request) {
        ProjectEntity project = findProjectAndVerifyOwner(userId, projectId);
        validateSkill(request.skillId());
        validateCompensation(request.dailyRate(), request.budgetAmount());
        WorkerType workerType = parseWorkerType(request.workerType());

        Instant now = clock.instant();
        WorkforceRequirementEntity entity = new WorkforceRequirementEntity(
            UUID.randomUUID(),
            project.getId(),
            request.location().trim(),
            request.startDate(),
            request.durationDays(),
            workerType,
            request.skillId(),
            request.quantity(),
            request.currencyCode() != null && !request.currencyCode().isBlank() ? request.currencyCode().trim().toUpperCase() : "INR",
            request.accommodationAvailable(),
            request.foodAvailable(),
            RequirementStatus.DRAFT,
            now
        );

        entity.setDailyRate(request.dailyRate());
        entity.setBudgetAmount(request.budgetAmount());
        entity.setAdditionalNotes(request.additionalNotes());

        return toResponse(requirementRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<WorkforceRequirementResponse> getRequirementsByProject(UUID userId, UUID projectId) {
        ProjectEntity project = findProjectAndVerifyOwner(userId, projectId);
        return requirementRepository.findByProjectId(project.getId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public WorkforceRequirementResponse getRequirementById(UUID userId, UUID requirementId) {
        WorkforceRequirementEntity entity = findRequirementAndVerifyOwner(userId, requirementId);
        return toResponse(entity);
    }

    @Transactional
    public WorkforceRequirementResponse updateRequirement(UUID userId, UUID requirementId, UpdateRequirementRequest request) {
        WorkforceRequirementEntity entity = findRequirementAndVerifyOwner(userId, requirementId);

        if (entity.getStatus() != RequirementStatus.DRAFT) {
            throw new WorkforceRequirementException("Requirement can only be updated while in DRAFT status.");
        }

        validateSkill(request.skillId());
        validateCompensation(request.dailyRate(), request.budgetAmount());
        WorkerType workerType = parseWorkerType(request.workerType());

        entity.setLocation(request.location().trim());
        entity.setStartDate(request.startDate());
        entity.setDurationDays(request.durationDays());
        entity.setWorkerType(workerType);
        entity.setSkillId(request.skillId());
        entity.setQuantity(request.quantity());
        entity.setDailyRate(request.dailyRate());
        entity.setBudgetAmount(request.budgetAmount());

        if (request.currencyCode() != null && !request.currencyCode().isBlank()) {
            entity.setCurrencyCode(request.currencyCode().trim().toUpperCase());
        }
        if (request.accommodationAvailable() != null) {
            entity.setAccommodationAvailable(request.accommodationAvailable());
        }
        if (request.foodAvailable() != null) {
            entity.setFoodAvailable(request.foodAvailable());
        }
        entity.setAdditionalNotes(request.additionalNotes());
        entity.setUpdatedAt(clock.instant());

        return toResponse(requirementRepository.save(entity));
    }

    @Transactional
    public WorkforceRequirementResponse openRequirement(UUID userId, UUID requirementId) {
        WorkforceRequirementEntity entity = findRequirementAndVerifyOwner(userId, requirementId);

        if (entity.getStatus() != RequirementStatus.DRAFT) {
            throw new WorkforceRequirementException("Only DRAFT requirements can be opened.");
        }

        entity.setStatus(RequirementStatus.OPEN);
        entity.setUpdatedAt(clock.instant());
        return toResponse(requirementRepository.save(entity));
    }

    @Transactional
    public WorkforceRequirementResponse cancelRequirement(UUID userId, UUID requirementId) {
        WorkforceRequirementEntity entity = findRequirementAndVerifyOwner(userId, requirementId);

        if (entity.getStatus() != RequirementStatus.DRAFT && entity.getStatus() != RequirementStatus.OPEN) {
            throw new WorkforceRequirementException("Cannot cancel requirement in status: " + entity.getStatus());
        }

        entity.setStatus(RequirementStatus.CANCELLED);
        entity.setUpdatedAt(clock.instant());
        return toResponse(requirementRepository.save(entity));
    }

    private ProjectEntity findProjectAndVerifyOwner(UUID userId, UUID projectId) {
        ClientProfileEntity clientProfile = clientProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new WorkforceRequirementException("Client profile not found."));

        ProjectEntity project = projectRepository.findById(projectId)
            .orElseThrow(() -> new WorkforceRequirementException("Project not found."));

        if (!project.getClientProfileId().equals(clientProfile.getId())) {
            throw new WorkforceRequirementException("Access denied. You do not own this project.");
        }

        return project;
    }

    private WorkforceRequirementEntity findRequirementAndVerifyOwner(UUID userId, UUID requirementId) {
        WorkforceRequirementEntity entity = requirementRepository.findById(requirementId)
            .orElseThrow(() -> new WorkforceRequirementException("Workforce requirement not found."));

        // Verify project ownership for this requirement
        findProjectAndVerifyOwner(userId, entity.getProjectId());
        return entity;
    }

    private void validateSkill(UUID skillId) {
        if (!skillRepository.findByIdAndIsActiveTrue(skillId).isPresent()) {
            throw new WorkforceRequirementException("Skill not found or inactive.");
        }
    }

    private void validateCompensation(BigDecimal dailyRate, BigDecimal budgetAmount) {
        boolean hasDaily = dailyRate != null;
        boolean hasBudget = budgetAmount != null;

        if (hasDaily == hasBudget) {
            throw new WorkforceRequirementException("Must specify exactly one of daily rate or total budget amount.");
        }

        if (hasDaily && dailyRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new WorkforceRequirementException("Daily rate cannot be negative.");
        }

        if (hasBudget && budgetAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new WorkforceRequirementException("Budget amount cannot be negative.");
        }
    }

    private WorkerType parseWorkerType(String raw) {
        try {
            return WorkerType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new WorkforceRequirementException("Invalid worker type. Allowed values: SKILLED_WORKER, LABOUR.");
        }
    }

    private WorkforceRequirementResponse toResponse(WorkforceRequirementEntity entity) {
        return new WorkforceRequirementResponse(
            entity.getId(),
            entity.getProjectId(),
            entity.getLocation(),
            entity.getStartDate(),
            entity.getDurationDays(),
            entity.getWorkerType().name(),
            entity.getSkillId(),
            entity.getQuantity(),
            entity.getDailyRate(),
            entity.getBudgetAmount(),
            entity.getCurrencyCode(),
            entity.getAccommodationAvailable(),
            entity.getFoodAvailable(),
            entity.getAdditionalNotes(),
            entity.getStatus().name(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
