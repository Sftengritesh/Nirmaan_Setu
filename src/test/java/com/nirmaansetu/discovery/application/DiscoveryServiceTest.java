package com.nirmaansetu.discovery.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.nirmaansetu.contractor.infrastructure.persistence.ContractorProfileEntity;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorProfileRepository;
import com.nirmaansetu.discovery.api.ContractorDiscoveryResponse;
import com.nirmaansetu.discovery.api.RequirementDiscoveryResponse;
import com.nirmaansetu.discovery.api.TeamDiscoveryResponse;
import com.nirmaansetu.discovery.api.WorkerDiscoveryResponse;
import com.nirmaansetu.requirement.domain.RequirementStatus;
import com.nirmaansetu.requirement.domain.WorkerType;
import com.nirmaansetu.requirement.infrastructure.persistence.WorkforceRequirementEntity;
import com.nirmaansetu.requirement.infrastructure.persistence.WorkforceRequirementRepository;
import com.nirmaansetu.team.domain.TeamStatus;
import com.nirmaansetu.team.infrastructure.persistence.TeamEntity;
import com.nirmaansetu.team.infrastructure.persistence.TeamMemberRepository;
import com.nirmaansetu.team.infrastructure.persistence.TeamRepository;
import com.nirmaansetu.verification.domain.SubjectType;
import com.nirmaansetu.verification.domain.VerificationStatus;
import com.nirmaansetu.verification.infrastructure.persistence.VerificationEntity;
import com.nirmaansetu.verification.infrastructure.persistence.VerificationRepository;
import com.nirmaansetu.worker.domain.AvailabilityStatus;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileEntity;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileRepository;
import java.time.Instant;
import java.time.LocalDate;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DiscoveryServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-27T00:00:00Z");

    @Mock private WorkerProfileRepository workerProfileRepository;
    @Mock private ContractorProfileRepository contractorProfileRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private WorkforceRequirementRepository requirementRepository;
    @Mock private VerificationRepository verificationRepository;

    private DiscoveryService discoveryService;

    @BeforeEach
    void setUp() {
        discoveryService = new DiscoveryService(
                workerProfileRepository,
                contractorProfileRepository,
                teamRepository,
                teamMemberRepository,
                requirementRepository,
                verificationRepository
        );
    }

    @Test
    void searchWorkersByLocationAndAvailability() {
        WorkerProfileEntity worker1 = new WorkerProfileEntity(UUID.randomUUID(), UUID.randomUUID(), "Alice", "Mohali", AvailabilityStatus.AVAILABLE, true, NOW);
        WorkerProfileEntity worker2 = new WorkerProfileEntity(UUID.randomUUID(), UUID.randomUUID(), "Bob", "Chandigarh", AvailabilityStatus.AVAILABLE, false, NOW);
        WorkerProfileEntity worker3 = new WorkerProfileEntity(UUID.randomUUID(), UUID.randomUUID(), "Charlie", "Mohali", AvailabilityStatus.UNAVAILABLE, true, NOW);

        when(workerProfileRepository.findAll()).thenReturn(List.of(worker1, worker2, worker3));

        Page<WorkerDiscoveryResponse> page = discoveryService.searchWorkers(
                "Mohali", null, AvailabilityStatus.AVAILABLE, null, null, null, null, PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).displayName()).isEqualTo("Alice");
    }

    @Test
    void searchWorkersEnforcesMaxPageSize() {
        WorkerProfileEntity worker = new WorkerProfileEntity(UUID.randomUUID(), UUID.randomUUID(), "Alice", "Delhi", AvailabilityStatus.AVAILABLE, true, NOW);
        when(workerProfileRepository.findAll()).thenReturn(List.of(worker));

        Pageable overSized = PageRequest.of(0, 100);
        Page<WorkerDiscoveryResponse> page = discoveryService.searchWorkers(
                null, null, null, null, null, null, null, overSized
        );

        assertThat(page.getPageable().getPageSize()).isEqualTo(50);
    }

    @Test
    void searchContractorsByLocationAndName() {
        ContractorProfileEntity contractor1 = new ContractorProfileEntity(UUID.randomUUID(), UUID.randomUUID(), "Alpha Builders", "Mohali", NOW);
        ContractorProfileEntity contractor2 = new ContractorProfileEntity(UUID.randomUUID(), UUID.randomUUID(), "Beta Infra", "Delhi", NOW);

        when(contractorProfileRepository.findAll()).thenReturn(List.of(contractor1, contractor2));

        Page<ContractorDiscoveryResponse> page = discoveryService.searchContractors(
                "Mohali", "Alpha", null, PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).displayName()).isEqualTo("Alpha Builders");
    }

    @Test
    void searchTeamsOnlyReturnsActiveTeams() {
        TeamEntity activeTeam = new TeamEntity(UUID.randomUUID(), UUID.randomUUID(), "Team Eagle", TeamStatus.ACTIVE, NOW);
        TeamEntity inactiveTeam = new TeamEntity(UUID.randomUUID(), UUID.randomUUID(), "Team Falcon", TeamStatus.INACTIVE, NOW);

        when(teamRepository.findAll()).thenReturn(List.of(activeTeam, inactiveTeam));
        when(teamMemberRepository.findByIdTeamId(activeTeam.getId())).thenReturn(List.of());

        Page<TeamDiscoveryResponse> page = discoveryService.searchTeams(null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).name()).isEqualTo("Team Eagle");
    }

    @Test
    void searchRequirementsOnlyReturnsOpenRequirements() {
        UUID skillId = UUID.randomUUID();
        WorkforceRequirementEntity openReq = new WorkforceRequirementEntity(
                UUID.randomUUID(), UUID.randomUUID(), "Mohali", LocalDate.of(2026, 9, 1), 10,
                WorkerType.SKILLED_WORKER, skillId, 5, "INR", false, false, RequirementStatus.OPEN, NOW
        );
        WorkforceRequirementEntity draftReq = new WorkforceRequirementEntity(
                UUID.randomUUID(), UUID.randomUUID(), "Mohali", LocalDate.of(2026, 9, 1), 10,
                WorkerType.SKILLED_WORKER, skillId, 5, "INR", false, false, RequirementStatus.DRAFT, NOW
        );
        WorkforceRequirementEntity fulfilledReq = new WorkforceRequirementEntity(
                UUID.randomUUID(), UUID.randomUUID(), "Mohali", LocalDate.of(2026, 9, 1), 10,
                WorkerType.SKILLED_WORKER, skillId, 5, "INR", false, false, RequirementStatus.FULFILLED, NOW
        );
        WorkforceRequirementEntity cancelledReq = new WorkforceRequirementEntity(
                UUID.randomUUID(), UUID.randomUUID(), "Mohali", LocalDate.of(2026, 9, 1), 10,
                WorkerType.SKILLED_WORKER, skillId, 5, "INR", false, false, RequirementStatus.CANCELLED, NOW
        );

        when(requirementRepository.findAll()).thenReturn(List.of(openReq, draftReq, fulfilledReq, cancelledReq));

        Page<RequirementDiscoveryResponse> page = discoveryService.searchRequirements(
                null, null, null, null, null, null, null, PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).status()).isEqualTo("OPEN");
        assertThat(page.getContent().get(0).id()).isEqualTo(openReq.getId());
    }

    @Test
    void searchWorkersReturnsVerifiedStatusFlag() {
        UUID workerId = UUID.randomUUID();
        WorkerProfileEntity worker = new WorkerProfileEntity(workerId, UUID.randomUUID(), "Verified Worker", "Delhi", AvailabilityStatus.AVAILABLE, true, NOW);
        VerificationEntity ver = new VerificationEntity(UUID.randomUUID(), SubjectType.WORKER, VerificationStatus.VERIFIED, NOW);
        ver.setSubjectWorkerProfileId(workerId);

        when(workerProfileRepository.findAll()).thenReturn(List.of(worker));
        when(verificationRepository.findFirstBySubjectWorkerProfileIdAndStatus(workerId, VerificationStatus.VERIFIED))
                .thenReturn(Optional.of(ver));

        Page<WorkerDiscoveryResponse> page = discoveryService.searchWorkers(
                null, null, null, null, null, null, true, PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).isVerified()).isTrue();
    }
}
