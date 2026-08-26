package com.nirmaansetu.team.api;

import com.nirmaansetu.auth.application.AuthPrincipal;
import com.nirmaansetu.team.application.TeamService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
@PreAuthorize("hasRole('CONTRACTOR')")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateTeamRequest request) {
        TeamResponse response = teamService.createTeam(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<TeamResponse> getMyTeams(@AuthenticationPrincipal AuthPrincipal principal) {
        return teamService.getMyTeams(principal.userId());
    }

    @GetMapping("/{teamId}")
    public TeamResponse getTeamById(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID teamId) {
        return teamService.getTeamById(principal.userId(), teamId);
    }

    @PutMapping("/{teamId}")
    public TeamResponse updateTeam(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID teamId,
            @Valid @RequestBody UpdateTeamRequest request) {
        return teamService.updateTeam(principal.userId(), teamId, request);
    }

    @PostMapping("/{teamId}/members")
    public ResponseEntity<TeamMemberResponse> addTeamMember(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID teamId,
            @Valid @RequestBody AddTeamMemberRequest request) {
        TeamMemberResponse response = teamService.addTeamMember(principal.userId(), teamId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{teamId}/members")
    public List<TeamMemberResponse> getTeamMembers(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID teamId) {
        return teamService.getTeamMembers(principal.userId(), teamId);
    }

    @PostMapping("/{teamId}/members/{workerProfileId}/end")
    public TeamMemberResponse removeTeamMember(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID teamId,
            @PathVariable UUID workerProfileId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startsOn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endsOn) {
        return teamService.removeTeamMember(principal.userId(), teamId, workerProfileId, startsOn, endsOn);
    }
}
