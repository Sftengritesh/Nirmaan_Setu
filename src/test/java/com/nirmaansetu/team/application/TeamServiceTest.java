package com.nirmaansetu.team.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nirmaansetu.team.api.AddTeamMemberRequest;
import com.nirmaansetu.team.api.CreateTeamRequest;
import com.nirmaansetu.team.api.TeamMemberResponse;
import com.nirmaansetu.team.api.TeamResponse;
import com.nirmaansetu.team.api.UpdateTeamRequest;
import com.nirmaansetu.team.domain.TeamException;
import com.nirmaansetu.team.domain.TeamStatus;
import com.nirmaansetu.team.infrastructure.persistence.TeamEntity;
import com.nirmaansetu.team.infrastructure.persistence.TeamMemberEntity;
import com.nirmaansetu.team.infrastructure.persistence.TeamMemberId;
import com.nirmaansetu.team.infrastructure.persistence.TeamMemberRepository;
import com.nirmaansetu.team.infrastructure.persistence.TeamRepository;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileRepository;
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
class TeamServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-26T10:00:00Z");

    @Mock private TeamRepository teamRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private WorkerProfileRepository workerProfileRepository;

    private TeamService service;

    @BeforeEach
    void setUp() {
        service = new TeamService(
            teamRepository, teamMemberRepository, workerProfileRepository, Clock.fixed(NOW, ZoneOffset.UTC)
        );
        when(teamRepository.save(any(TeamEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(teamMemberRepository.save(any(TeamMemberEntity.class))).thenAnswer(i -> i.getArgument(0));
    }

    private TeamEntity teamFor(UUID managerUserId) {
        TeamEntity entity = new TeamEntity(UUID.randomUUID(), managerUserId, "Masonry Crew A", TeamStatus.ACTIVE, NOW);
        entity.setDescription("Primary brickwork team");
        return entity;
    }

    @Test
    void managerCanCreateTeam() {
        UUID managerUserId = UUID.randomUUID();
        CreateTeamRequest req = new CreateTeamRequest("Masonry Crew A", "Primary brickwork team");

        TeamResponse response = service.createTeam(managerUserId, req);

        assertThat(response.managerUserId()).isEqualTo(managerUserId);
        assertThat(response.name()).isEqualTo("Masonry Crew A");
        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    void getMyTeamsReturnsList() {
        UUID managerUserId = UUID.randomUUID();
        TeamEntity team = teamFor(managerUserId);
        when(teamRepository.findByManagerUserId(managerUserId)).thenReturn(List.of(team));

        List<TeamResponse> teams = service.getMyTeams(managerUserId);

        assertThat(teams).hasSize(1);
        assertThat(teams.get(0).name()).isEqualTo("Masonry Crew A");
    }

    @Test
    void getTeamByIdSuccess() {
        UUID managerUserId = UUID.randomUUID();
        TeamEntity team = teamFor(managerUserId);
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        TeamResponse response = service.getTeamById(managerUserId, team.getId());

        assertThat(response.id()).isEqualTo(team.getId());
        assertThat(response.name()).isEqualTo("Masonry Crew A");
    }

    @Test
    void getTeamByIdFailsForNonManager() {
        UUID managerUserId = UUID.randomUUID();
        UUID strangerUserId = UUID.randomUUID();
        TeamEntity team = teamFor(managerUserId);
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> service.getTeamById(strangerUserId, team.getId()))
            .isInstanceOf(TeamException.class)
            .hasMessageContaining("Access denied");
    }

    @Test
    void updateTeamSuccess() {
        UUID managerUserId = UUID.randomUUID();
        TeamEntity team = teamFor(managerUserId);
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        UpdateTeamRequest update = new UpdateTeamRequest("Masonry Crew Alpha", "Updated description", "INACTIVE");

        TeamResponse response = service.updateTeam(managerUserId, team.getId(), update);

        assertThat(response.name()).isEqualTo("Masonry Crew Alpha");
        assertThat(response.status()).isEqualTo("INACTIVE");
    }

    @Test
    void addTeamMemberSuccess() {
        UUID managerUserId = UUID.randomUUID();
        UUID workerProfileId = UUID.randomUUID();
        TeamEntity team = teamFor(managerUserId);

        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(workerProfileRepository.existsById(workerProfileId)).thenReturn(true);

        AddTeamMemberRequest req = new AddTeamMemberRequest(
            workerProfileId, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 31)
        );

        TeamMemberResponse response = service.addTeamMember(managerUserId, team.getId(), req);

        assertThat(response.teamId()).isEqualTo(team.getId());
        assertThat(response.workerProfileId()).isEqualTo(workerProfileId);
        assertThat(response.startsOn()).isEqualTo(LocalDate.of(2026, 9, 1));
    }

    @Test
    void addTeamMemberFailsIfInactiveTeam() {
        UUID managerUserId = UUID.randomUUID();
        UUID workerProfileId = UUID.randomUUID();
        TeamEntity team = teamFor(managerUserId);
        team.setStatus(TeamStatus.INACTIVE);

        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        AddTeamMemberRequest req = new AddTeamMemberRequest(
            workerProfileId, LocalDate.of(2026, 9, 1), null
        );

        assertThatThrownBy(() -> service.addTeamMember(managerUserId, team.getId(), req))
            .isInstanceOf(TeamException.class)
            .hasMessageContaining("inactive team");
    }

    @Test
    void addTeamMemberFailsIfWorkerNotFound() {
        UUID managerUserId = UUID.randomUUID();
        UUID workerProfileId = UUID.randomUUID();
        TeamEntity team = teamFor(managerUserId);

        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(workerProfileRepository.existsById(workerProfileId)).thenReturn(false);

        AddTeamMemberRequest req = new AddTeamMemberRequest(
            workerProfileId, LocalDate.of(2026, 9, 1), null
        );

        assertThatThrownBy(() -> service.addTeamMember(managerUserId, team.getId(), req))
            .isInstanceOf(TeamException.class)
            .hasMessageContaining("Worker profile not found");
    }

    @Test
    void addTeamMemberFailsIfEndBeforeStart() {
        UUID managerUserId = UUID.randomUUID();
        UUID workerProfileId = UUID.randomUUID();
        TeamEntity team = teamFor(managerUserId);

        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(workerProfileRepository.existsById(workerProfileId)).thenReturn(true);

        AddTeamMemberRequest req = new AddTeamMemberRequest(
            workerProfileId, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 8, 1)
        );

        assertThatThrownBy(() -> service.addTeamMember(managerUserId, team.getId(), req))
            .isInstanceOf(TeamException.class)
            .hasMessageContaining("End date cannot be before start date");
    }

    @Test
    void removeTeamMemberSuccess() {
        UUID managerUserId = UUID.randomUUID();
        UUID workerProfileId = UUID.randomUUID();
        TeamEntity team = teamFor(managerUserId);
        LocalDate startsOn = LocalDate.of(2026, 9, 1);

        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        TeamMemberId id = new TeamMemberId(team.getId(), workerProfileId, startsOn);
        TeamMemberEntity member = new TeamMemberEntity(id, null, NOW);
        when(teamMemberRepository.findById(id)).thenReturn(Optional.of(member));

        LocalDate endsOn = LocalDate.of(2026, 11, 30);
        TeamMemberResponse response = service.removeTeamMember(managerUserId, team.getId(), workerProfileId, startsOn, endsOn);

        assertThat(response.endsOn()).isEqualTo(endsOn);
        org.mockito.Mockito.verify(teamMemberRepository, org.mockito.Mockito.never()).delete(any());
    }

    @Test
    void getTeamMembersReturnsList() {
        UUID managerUserId = UUID.randomUUID();
        UUID workerProfileId = UUID.randomUUID();
        TeamEntity team = teamFor(managerUserId);

        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        TeamMemberId id = new TeamMemberId(team.getId(), workerProfileId, LocalDate.of(2026, 9, 1));
        TeamMemberEntity member = new TeamMemberEntity(id, null, NOW);
        when(teamMemberRepository.findByIdTeamId(team.getId())).thenReturn(List.of(member));

        List<TeamMemberResponse> members = service.getTeamMembers(managerUserId, team.getId());

        assertThat(members).hasSize(1);
        assertThat(members.get(0).workerProfileId()).isEqualTo(workerProfileId);
    }
}
