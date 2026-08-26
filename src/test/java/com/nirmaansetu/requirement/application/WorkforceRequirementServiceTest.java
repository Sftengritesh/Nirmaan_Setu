package com.nirmaansetu.requirement.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nirmaansetu.client.domain.ClientType;
import com.nirmaansetu.client.infrastructure.persistence.ClientProfileEntity;
import com.nirmaansetu.client.infrastructure.persistence.ClientProfileRepository;
import com.nirmaansetu.project.domain.ProjectStatus;
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
import com.nirmaansetu.worker.infrastructure.persistence.SkillEntity;
import com.nirmaansetu.worker.infrastructure.persistence.SkillRepository;
import java.math.BigDecimal;
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
class WorkforceRequirementServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-26T10:00:00Z");

    @Mock
    private WorkforceRequirementRepository requirementRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ClientProfileRepository clientProfileRepository;

    @Mock
    private SkillRepository skillRepository;

    private WorkforceRequirementService requirementService;

    @BeforeEach
    void setUp() {
        requirementService = new WorkforceRequirementService(
            requirementRepository, projectRepository, clientProfileRepository, skillRepository, Clock.fixed(NOW, ZoneOffset.UTC)
        );
        when(requirementRepository.save(any(WorkforceRequirementEntity.class))).thenAnswer(i -> i.getArgument(0));
    }

    private ClientProfileEntity clientProfileFor(UUID userId) {
        return new ClientProfileEntity(UUID.randomUUID(), userId, ClientType.HOMEOWNER, "Jane Doe", NOW);
    }

    private ProjectEntity projectFor(UUID clientProfileId) {
        return new ProjectEntity(UUID.randomUUID(), clientProfileId, "Villa Project", ProjectStatus.ACTIVE, "Mohali", NOW);
    }

    private SkillEntity skillFor(UUID skillId, boolean active) {
        SkillEntity skill = org.mockito.Mockito.mock(SkillEntity.class);
        when(skill.getId()).thenReturn(skillId);
        when(skill.isActive()).thenReturn(active);
        return skill;
    }

    private WorkforceRequirementEntity requirementFor(UUID projectId, UUID skillId, RequirementStatus status) {
        WorkforceRequirementEntity req = new WorkforceRequirementEntity(
            UUID.randomUUID(), projectId, "Mohali", LocalDate.of(2026, 9, 1), 30,
            WorkerType.SKILLED_WORKER, skillId, 2, "INR", false, false, status, NOW
        );
        req.setDailyRate(new BigDecimal("1500.00"));
        return req;
    }

    @Test
    void createRequirementSuccessWithDailyRate() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        ClientProfileEntity clientProfile = clientProfileFor(userId);
        ProjectEntity project = projectFor(clientProfile.getId());
        SkillEntity skill = skillFor(skillId, true);

        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(clientProfile));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(skillRepository.findByIdAndIsActiveTrue(skillId)).thenReturn(Optional.of(skill));

        CreateRequirementRequest request = new CreateRequirementRequest(
            "Mohali", LocalDate.of(2026, 9, 1), 30, "SKILLED_WORKER", skillId, 2,
            new BigDecimal("1500.00"), null, "INR", false, false, "Needs experienced electrician"
        );

        WorkforceRequirementResponse response = requirementService.createRequirement(userId, project.getId(), request);

        assertThat(response.projectId()).isEqualTo(project.getId());
        assertThat(response.status()).isEqualTo("DRAFT");
        assertThat(response.dailyRate()).isEqualTo(new BigDecimal("1500.00"));
        assertThat(response.budgetAmount()).isNull();
    }

    @Test
    void createRequirementSuccessWithBudgetAmount() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        ClientProfileEntity clientProfile = clientProfileFor(userId);
        ProjectEntity project = projectFor(clientProfile.getId());
        SkillEntity skill = skillFor(skillId, true);

        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(clientProfile));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(skillRepository.findByIdAndIsActiveTrue(skillId)).thenReturn(Optional.of(skill));

        CreateRequirementRequest request = new CreateRequirementRequest(
            "Mohali", LocalDate.of(2026, 9, 1), 30, "LABOUR", skillId, 5,
            null, new BigDecimal("45000.00"), "INR", true, true, null
        );

        WorkforceRequirementResponse response = requirementService.createRequirement(userId, project.getId(), request);

        assertThat(response.status()).isEqualTo("DRAFT");
        assertThat(response.dailyRate()).isNull();
        assertThat(response.budgetAmount()).isEqualTo(new BigDecimal("45000.00"));
    }

    @Test
    void compensationXorRuleFailsWhenBothProvided() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        ClientProfileEntity clientProfile = clientProfileFor(userId);
        ProjectEntity project = projectFor(clientProfile.getId());
        SkillEntity skill = skillFor(skillId, true);

        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(clientProfile));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(skillRepository.findByIdAndIsActiveTrue(skillId)).thenReturn(Optional.of(skill));

        CreateRequirementRequest request = new CreateRequirementRequest(
            "Mohali", LocalDate.of(2026, 9, 1), 30, "LABOUR", skillId, 5,
            new BigDecimal("1500.00"), new BigDecimal("45000.00"), "INR", false, false, null
        );

        assertThatThrownBy(() -> requirementService.createRequirement(userId, project.getId(), request))
            .isInstanceOf(WorkforceRequirementException.class)
            .hasMessageContaining("Must specify exactly one of daily rate or total budget amount");
    }

    @Test
    void compensationXorRuleFailsWhenNeitherProvided() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        ClientProfileEntity clientProfile = clientProfileFor(userId);
        ProjectEntity project = projectFor(clientProfile.getId());
        SkillEntity skill = skillFor(skillId, true);

        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(clientProfile));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(skillRepository.findByIdAndIsActiveTrue(skillId)).thenReturn(Optional.of(skill));

        CreateRequirementRequest request = new CreateRequirementRequest(
            "Mohali", LocalDate.of(2026, 9, 1), 30, "LABOUR", skillId, 5,
            null, null, "INR", false, false, null
        );

        assertThatThrownBy(() -> requirementService.createRequirement(userId, project.getId(), request))
            .isInstanceOf(WorkforceRequirementException.class)
            .hasMessageContaining("Must specify exactly one of daily rate or total budget amount");
    }

    @Test
    void inactiveSkillRejected() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        ClientProfileEntity clientProfile = clientProfileFor(userId);
        ProjectEntity project = projectFor(clientProfile.getId());

        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(clientProfile));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(skillRepository.findByIdAndIsActiveTrue(skillId)).thenReturn(Optional.empty());

        CreateRequirementRequest request = new CreateRequirementRequest(
            "Mohali", LocalDate.of(2026, 9, 1), 30, "LABOUR", skillId, 5,
            new BigDecimal("1000.00"), null, "INR", false, false, null
        );

        assertThatThrownBy(() -> requirementService.createRequirement(userId, project.getId(), request))
            .isInstanceOf(WorkforceRequirementException.class)
            .hasMessageContaining("Skill not found or inactive");
    }

    @Test
    void crossClientAccessBlocked() {
        UUID clientAUserId = UUID.randomUUID();
        UUID clientBUserId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        ClientProfileEntity clientAProfile = clientProfileFor(clientAUserId);
        ClientProfileEntity clientBProfile = clientProfileFor(clientBUserId);
        ProjectEntity projectA = projectFor(clientAProfile.getId());

        when(clientProfileRepository.findByUserId(clientBUserId)).thenReturn(Optional.of(clientBProfile));
        when(projectRepository.findById(projectA.getId())).thenReturn(Optional.of(projectA));

        CreateRequirementRequest request = new CreateRequirementRequest(
            "Mohali", LocalDate.of(2026, 9, 1), 30, "LABOUR", skillId, 5,
            new BigDecimal("1000.00"), null, "INR", false, false, null
        );

        // Client B tries to create requirement for Client A's project
        assertThatThrownBy(() -> requirementService.createRequirement(clientBUserId, projectA.getId(), request))
            .isInstanceOf(WorkforceRequirementException.class)
            .hasMessageContaining("Access denied");
    }

    @Test
    void draftRequirementCanBeUpdated() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        ClientProfileEntity clientProfile = clientProfileFor(userId);
        ProjectEntity project = projectFor(clientProfile.getId());
        SkillEntity skill = skillFor(skillId, true);
        WorkforceRequirementEntity requirement = requirementFor(project.getId(), skillId, RequirementStatus.DRAFT);

        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(clientProfile));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(requirementRepository.findById(requirement.getId())).thenReturn(Optional.of(requirement));
        when(skillRepository.findByIdAndIsActiveTrue(skillId)).thenReturn(Optional.of(skill));

        UpdateRequirementRequest update = new UpdateRequirementRequest(
            "Chandigarh", LocalDate.of(2026, 9, 10), 45, "SKILLED_WORKER", skillId, 3,
            new BigDecimal("1800.00"), null, "INR", true, true, "Updated notes"
        );

        WorkforceRequirementResponse response = requirementService.updateRequirement(userId, requirement.getId(), update);

        assertThat(response.location()).isEqualTo("Chandigarh");
        assertThat(response.quantity()).isEqualTo(3);
        assertThat(response.dailyRate()).isEqualTo(new BigDecimal("1800.00"));
    }

    @Test
    void openRequirementCannotBeUpdated() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        ClientProfileEntity clientProfile = clientProfileFor(userId);
        ProjectEntity project = projectFor(clientProfile.getId());
        SkillEntity skill = skillFor(skillId, true);
        WorkforceRequirementEntity requirement = requirementFor(project.getId(), skillId, RequirementStatus.OPEN);

        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(clientProfile));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(requirementRepository.findById(requirement.getId())).thenReturn(Optional.of(requirement));
        when(skillRepository.findByIdAndIsActiveTrue(skillId)).thenReturn(Optional.of(skill));

        UpdateRequirementRequest update = new UpdateRequirementRequest(
            "Chandigarh", LocalDate.of(2026, 9, 10), 45, "SKILLED_WORKER", skillId, 3,
            new BigDecimal("1800.00"), null, "INR", true, true, "Updated notes"
        );

        assertThatThrownBy(() -> requirementService.updateRequirement(userId, requirement.getId(), update))
            .isInstanceOf(WorkforceRequirementException.class)
            .hasMessageContaining("can only be updated while in DRAFT status");
    }

    @Test
    void fulfilledAndCancelledRequirementsCannotBeUpdated() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        ClientProfileEntity clientProfile = clientProfileFor(userId);
        ProjectEntity project = projectFor(clientProfile.getId());
        SkillEntity skill = skillFor(skillId, true);

        WorkforceRequirementEntity fulfilled = requirementFor(project.getId(), skillId, RequirementStatus.FULFILLED);
        WorkforceRequirementEntity cancelled = requirementFor(project.getId(), skillId, RequirementStatus.CANCELLED);

        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(clientProfile));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(skillRepository.findByIdAndIsActiveTrue(skillId)).thenReturn(Optional.of(skill));

        UpdateRequirementRequest update = new UpdateRequirementRequest(
            "Chandigarh", LocalDate.of(2026, 9, 10), 45, "SKILLED_WORKER", skillId, 3,
            new BigDecimal("1800.00"), null, "INR", true, true, null
        );

        when(requirementRepository.findById(fulfilled.getId())).thenReturn(Optional.of(fulfilled));
        assertThatThrownBy(() -> requirementService.updateRequirement(userId, fulfilled.getId(), update))
            .isInstanceOf(WorkforceRequirementException.class)
            .hasMessageContaining("can only be updated while in DRAFT status");

        when(requirementRepository.findById(cancelled.getId())).thenReturn(Optional.of(cancelled));
        assertThatThrownBy(() -> requirementService.updateRequirement(userId, cancelled.getId(), update))
            .isInstanceOf(WorkforceRequirementException.class)
            .hasMessageContaining("can only be updated while in DRAFT status");
    }

    @Test
    void draftToOpenStateTransitionSucceeds() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        ClientProfileEntity clientProfile = clientProfileFor(userId);
        ProjectEntity project = projectFor(clientProfile.getId());
        WorkforceRequirementEntity requirement = requirementFor(project.getId(), skillId, RequirementStatus.DRAFT);

        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(clientProfile));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(requirementRepository.findById(requirement.getId())).thenReturn(Optional.of(requirement));

        WorkforceRequirementResponse response = requirementService.openRequirement(userId, requirement.getId());

        assertThat(response.status()).isEqualTo("OPEN");
    }

    @Test
    void draftToCancelledAndOpenToCancelledStateTransitionsSucceed() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        ClientProfileEntity clientProfile = clientProfileFor(userId);
        ProjectEntity project = projectFor(clientProfile.getId());
        WorkforceRequirementEntity draft = requirementFor(project.getId(), skillId, RequirementStatus.DRAFT);
        WorkforceRequirementEntity open = requirementFor(project.getId(), skillId, RequirementStatus.OPEN);

        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(clientProfile));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        when(requirementRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
        WorkforceRequirementResponse response1 = requirementService.cancelRequirement(userId, draft.getId());
        assertThat(response1.status()).isEqualTo("CANCELLED");

        when(requirementRepository.findById(open.getId())).thenReturn(Optional.of(open));
        WorkforceRequirementResponse response2 = requirementService.cancelRequirement(userId, open.getId());
        assertThat(response2.status()).isEqualTo("CANCELLED");
    }
}
