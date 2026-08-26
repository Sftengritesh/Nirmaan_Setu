package com.nirmaansetu.project.api;

import com.nirmaansetu.auth.application.AuthPrincipal;
import com.nirmaansetu.project.application.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@PreAuthorize("hasRole('CLIENT')")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<ProjectResponse> getMyProjects(@AuthenticationPrincipal AuthPrincipal principal) {
        return projectService.getMyProjects(principal.userId());
    }

    @GetMapping("/{projectId}")
    public ProjectResponse getProjectById(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID projectId) {
        return projectService.getProjectById(principal.userId(), projectId);
    }

    @PutMapping("/{projectId}")
    public ProjectResponse updateProject(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request) {
        return projectService.updateProject(principal.userId(), projectId, request);
    }
}
