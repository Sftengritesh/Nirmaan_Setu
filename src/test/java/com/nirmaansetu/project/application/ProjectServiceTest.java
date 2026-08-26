package com.nirmaansetu.project.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nirmaansetu.client.domain.ClientType;
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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-26T10:00:00Z");

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ClientProfileRepository clientProfileRepository;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, clientProfileRepository, Clock.fixed(NOW, ZoneOffset.UTC));
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(i -> i.getArgument(0));
    }

    private ClientProfileEntity clientProfileFor(UUID userId) {
        return new ClientProfileEntity(UUID.randomUUID(), userId, ClientType.HOMEOWNER, "Jane Doe", NOW);
    }

    private ProjectEntity projectFor(UUID clientProfileId) {
        return new ProjectEntity(UUID.randomUUID(), clientProfileId, "Villa Construction", ProjectStatus.DRAFT, "Indiranagar, Bengaluru", NOW);
    }

    @Test
    void createProjectSuccessDefaultsToDraft() {
        UUID userId = UUID.randomUUID();
        ClientProfileEntity clientProfile = clientProfileFor(userId);
        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(clientProfile));

        CreateProjectRequest request = new CreateProjectRequest(
            "Villa Construction", "2-storey house", "Indiranagar, Bengaluru", LocalDate.of(2026, 9, 1)
        );

        ProjectResponse response = projectService.createProject(userId, request);

        assertThat(response.clientProfileId()).isEqualTo(clientProfile.getId());
        assertThat(response.title()).isEqualTo("Villa Construction");
        assertThat(response.status()).isEqualTo("DRAFT");
        assertThat(response.location()).isEqualTo("Indiranagar, Bengaluru");
    }

    @Test
    void createProjectFailsIfClientProfileNotFound() {
        UUID userId = UUID.randomUUID();
        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        CreateProjectRequest request = new CreateProjectRequest("Villa Construction", null, "Indiranagar", null);

        assertThatThrownBy(() -> projectService.createProject(userId, request))
            .isInstanceOf(ProjectException.class)
            .hasMessageContaining("Client profile not found");
    }

    @Test
    void getMyProjectsReturnsList() {
        UUID userId = UUID.randomUUID();
        ClientProfileEntity clientProfile = clientProfileFor(userId);
        ProjectEntity project = projectFor(clientProfile.getId());

        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(clientProfile));
        when(projectRepository.findByClientProfileId(clientProfile.getId())).thenReturn(List.of(project));

        List<ProjectResponse> list = projectService.getMyProjects(userId);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).title()).isEqualTo("Villa Construction");
    }

    @Test
    void getProjectByIdSuccess() {
        UUID userId = UUID.randomUUID();
        ClientProfileEntity clientProfile = clientProfileFor(userId);
        ProjectEntity project = projectFor(clientProfile.getId());

        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(clientProfile));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.getProjectById(userId, project.getId());

        assertThat(response.id()).isEqualTo(project.getId());
        assertThat(response.title()).isEqualTo("Villa Construction");
    }

    @Test
    void getProjectByIdFailsForCrossClientAccess() {
        UUID clientAUserId = UUID.randomUUID();
        UUID clientBUserId = UUID.randomUUID();

        ClientProfileEntity clientProfileA = clientProfileFor(clientAUserId);
        ClientProfileEntity clientProfileB = clientProfileFor(clientBUserId);
        ProjectEntity projectA = projectFor(clientProfileA.getId());

        // Client B tries to view Client A's project
        when(clientProfileRepository.findByUserId(clientBUserId)).thenReturn(Optional.of(clientProfileB));
        when(projectRepository.findById(projectA.getId())).thenReturn(Optional.of(projectA));

        assertThatThrownBy(() -> projectService.getProjectById(clientBUserId, projectA.getId()))
            .isInstanceOf(ProjectException.class)
            .hasMessageContaining("Access denied");
    }

    @Test
    void updateProjectSuccess() {
        UUID userId = UUID.randomUUID();
        ClientProfileEntity clientProfile = clientProfileFor(userId);
        ProjectEntity project = projectFor(clientProfile.getId());

        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(clientProfile));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        UpdateProjectRequest update = new UpdateProjectRequest(
            "Villa Construction Phase 1", "Updated desc", "Koramangala, Bengaluru", "ACTIVE", LocalDate.of(2026, 9, 15)
        );

        ProjectResponse response = projectService.updateProject(userId, project.getId(), update);

        assertThat(response.title()).isEqualTo("Villa Construction Phase 1");
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.location()).isEqualTo("Koramangala, Bengaluru");
    }

    @Test
    void updateProjectFailsForInvalidStatus() {
        UUID userId = UUID.randomUUID();
        ClientProfileEntity clientProfile = clientProfileFor(userId);
        ProjectEntity project = projectFor(clientProfile.getId());

        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(clientProfile));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        UpdateProjectRequest update = new UpdateProjectRequest(
            "Villa Construction", null, "Koramangala", "INVALID_STATUS", null
        );

        assertThatThrownBy(() -> projectService.updateProject(userId, project.getId(), update))
            .isInstanceOf(ProjectException.class)
            .hasMessageContaining("Invalid project status");
    }

    @Test
    void updateProjectFailsForCrossClientUpdate() {
        UUID clientAUserId = UUID.randomUUID();
        UUID clientBUserId = UUID.randomUUID();

        ClientProfileEntity clientProfileA = clientProfileFor(clientAUserId);
        ClientProfileEntity clientProfileB = clientProfileFor(clientBUserId);
        ProjectEntity projectA = projectFor(clientProfileA.getId());

        when(clientProfileRepository.findByUserId(clientBUserId)).thenReturn(Optional.of(clientProfileB));
        when(projectRepository.findById(projectA.getId())).thenReturn(Optional.of(projectA));

        UpdateProjectRequest update = new UpdateProjectRequest(
            "Hacked Title", null, "Unknown location", "ACTIVE", null
        );

        assertThatThrownBy(() -> projectService.updateProject(clientBUserId, projectA.getId(), update))
            .isInstanceOf(ProjectException.class)
            .hasMessageContaining("Access denied");
    }
}
