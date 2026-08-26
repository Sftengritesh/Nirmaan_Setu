package com.nirmaansetu.team.application;

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
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final Clock clock;

    public TeamService(TeamRepository teamRepository,
                       TeamMemberRepository teamMemberRepository,
                       WorkerProfileRepository workerProfileRepository,
                       Clock clock) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.workerProfileRepository = workerProfileRepository;
        this.clock = clock;
    }

    @Transactional
    public TeamResponse createTeam(UUID managerUserId, CreateTeamRequest request) {
        Instant now = clock.instant();
        TeamEntity entity = new TeamEntity(
            UUID.randomUUID(), managerUserId, request.name().trim(), TeamStatus.ACTIVE, now
        );
        entity.setDescription(request.description());
        return toTeamResponse(teamRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getMyTeams(UUID managerUserId) {
        return teamRepository.findByManagerUserId(managerUserId).stream()
            .map(this::toTeamResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TeamResponse getTeamById(UUID managerUserId, UUID teamId) {
        TeamEntity team = findTeamAndVerifyManager(managerUserId, teamId);
        return toTeamResponse(team);
    }

    @Transactional
    public TeamResponse updateTeam(UUID managerUserId, UUID teamId, UpdateTeamRequest request) {
        TeamEntity team = findTeamAndVerifyManager(managerUserId, teamId);
        team.setName(request.name().trim());
        team.setDescription(request.description());
        if (request.status() != null && !request.status().isBlank()) {
            team.setStatus(parseTeamStatus(request.status()));
        }
        team.setUpdatedAt(clock.instant());
        return toTeamResponse(teamRepository.save(team));
    }

    @Transactional
    public TeamMemberResponse addTeamMember(UUID managerUserId, UUID teamId, AddTeamMemberRequest request) {
        TeamEntity team = findTeamAndVerifyManager(managerUserId, teamId);
        if (team.getStatus() != TeamStatus.ACTIVE) {
            throw new TeamException("Cannot add members to an inactive team.");
        }

        if (!workerProfileRepository.existsById(request.workerProfileId())) {
            throw new TeamException("Worker profile not found.");
        }

        if (request.endsOn() != null && request.endsOn().isBefore(request.startsOn())) {
            throw new TeamException("End date cannot be before start date.");
        }

        TeamMemberId id = new TeamMemberId(team.getId(), request.workerProfileId(), request.startsOn());
        Instant now = clock.instant();
        TeamMemberEntity entity = new TeamMemberEntity(id, request.endsOn(), now);
        try {
            entity = teamMemberRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new TeamException("A team membership with this start date already exists.");
        }
        return toMemberResponse(entity);
    }

    @Transactional
    public TeamMemberResponse removeTeamMember(UUID managerUserId, UUID teamId, UUID workerProfileId, LocalDate startsOn, LocalDate endsOn) {
        TeamEntity team = findTeamAndVerifyManager(managerUserId, teamId);
        TeamMemberId id = new TeamMemberId(team.getId(), workerProfileId, startsOn);
        TeamMemberEntity entity = teamMemberRepository.findById(id)
            .orElseThrow(() -> new TeamException("Team member record not found."));

        if (endsOn != null && endsOn.isBefore(id.getStartsOn())) {
            throw new TeamException("End date cannot be before start date.");
        }

        entity.setEndsOn(endsOn);
        return toMemberResponse(teamMemberRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getTeamMembers(UUID managerUserId, UUID teamId) {
        TeamEntity team = findTeamAndVerifyManager(managerUserId, teamId);
        return teamMemberRepository.findByIdTeamId(team.getId()).stream()
            .map(this::toMemberResponse)
            .toList();
    }

    private TeamEntity findTeamAndVerifyManager(UUID managerUserId, UUID teamId) {
        TeamEntity team = teamRepository.findById(teamId)
            .orElseThrow(() -> new TeamException("Team not found."));
        if (!team.getManagerUserId().equals(managerUserId)) {
            throw new TeamException("Access denied. You are not the manager of this team.");
        }
        return team;
    }

    private TeamStatus parseTeamStatus(String raw) {
        try {
            return TeamStatus.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new TeamException("Invalid team status. Allowed values: ACTIVE, INACTIVE.");
        }
    }

    private TeamResponse toTeamResponse(TeamEntity entity) {
        return new TeamResponse(
            entity.getId(),
            entity.getManagerUserId(),
            entity.getName(),
            entity.getDescription(),
            entity.getStatus().name(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private TeamMemberResponse toMemberResponse(TeamMemberEntity entity) {
        return new TeamMemberResponse(
            entity.getId().getTeamId(),
            entity.getId().getWorkerProfileId(),
            entity.getId().getStartsOn(),
            entity.getEndsOn(),
            entity.getCreatedAt()
        );
    }
}
