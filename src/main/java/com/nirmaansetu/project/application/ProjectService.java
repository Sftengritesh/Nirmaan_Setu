package com.nirmaansetu.project.application;

import com.nirmaansetu.client.infrastructure.persistence.ClientProfileEntity;
import com.nirmaansetu.client.infrastructure.persistence.ClientProfileRepository;
import com.nirmaansetu.project.api.CreateProjectRequest;
import com.nirmaansetu.project.api.ProjectResponse;
import com.nirmaansetu.project.api.UpdateProjectRequest;
import com.nirmaansetu.project.domain.ProjectException;
import com.nirmaansetu.project.domain.ProjectStatus;
import com.nirmaansetu.project.infrastructure.persistence.ProjectEntity;
import com.nirmaansetu.project.infrastructure.persistence.ProjectRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final Clock clock;

    public ProjectService(ProjectRepository projectRepository,
                          ClientProfileRepository clientProfileRepository,
                          Clock clock) {
        this.projectRepository = projectRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.clock = clock;
    }

    @Transactional
    public ProjectResponse createProject(UUID userId, CreateProjectRequest request) {
        ClientProfileEntity clientProfile = resolveClientProfile(userId);
        Instant now = clock.instant();

        ProjectEntity entity = new ProjectEntity(
            UUID.randomUUID(),
            clientProfile.getId(),
            request.title().trim(),
            ProjectStatus.DRAFT,
            request.location().trim(),
            now
        );
        entity.setDescription(request.description());
        entity.setStartDate(request.startDate());

        return toResponse(projectRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getMyProjects(UUID userId) {
        ClientProfileEntity clientProfile = resolveClientProfile(userId);
        return projectRepository.findByClientProfileId(clientProfile.getId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID userId, UUID projectId) {
        ProjectEntity project = findProjectAndVerifyOwner(userId, projectId);
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID userId, UUID projectId, UpdateProjectRequest request) {
        ProjectEntity project = findProjectAndVerifyOwner(userId, projectId);

        project.setTitle(request.title().trim());
        project.setDescription(request.description());
        project.setLocation(request.location().trim());
        project.setStartDate(request.startDate());

        if (request.status() != null && !request.status().isBlank()) {
            project.setStatus(parseProjectStatus(request.status()));
        }

        project.setUpdatedAt(clock.instant());
        return toResponse(projectRepository.save(project));
    }

    private ClientProfileEntity resolveClientProfile(UUID userId) {
        return clientProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ProjectException("Client profile not found. Please create a client profile first."));
    }

    private ProjectEntity findProjectAndVerifyOwner(UUID userId, UUID projectId) {
        ClientProfileEntity clientProfile = resolveClientProfile(userId);
        ProjectEntity project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectException("Project not found."));

        if (!project.getClientProfileId().equals(clientProfile.getId())) {
            throw new ProjectException("Access denied. You do not own this project.");
        }

        return project;
    }

    private ProjectStatus parseProjectStatus(String raw) {
        try {
            return ProjectStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ProjectException("Invalid project status. Allowed values: DRAFT, ACTIVE, COMPLETED, CANCELLED.");
        }
    }

    private ProjectResponse toResponse(ProjectEntity entity) {
        return new ProjectResponse(
            entity.getId(),
            entity.getClientProfileId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getLocation(),
            entity.getStatus().name(),
            entity.getStartDate(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
