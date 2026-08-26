package com.nirmaansetu.requirement.api;

import com.nirmaansetu.auth.application.AuthPrincipal;
import com.nirmaansetu.requirement.application.WorkforceRequirementService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('CLIENT')")
public class WorkforceRequirementController {

    private final WorkforceRequirementService requirementService;

    public WorkforceRequirementController(WorkforceRequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @PostMapping("/api/projects/{projectId}/requirements")
    public ResponseEntity<WorkforceRequirementResponse> createRequirement(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateRequirementRequest request) {
        WorkforceRequirementResponse response = requirementService.createRequirement(principal.userId(), projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/projects/{projectId}/requirements")
    public List<WorkforceRequirementResponse> getRequirementsByProject(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID projectId) {
        return requirementService.getRequirementsByProject(principal.userId(), projectId);
    }

    @GetMapping("/api/requirements/{requirementId}")
    public WorkforceRequirementResponse getRequirementById(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID requirementId) {
        return requirementService.getRequirementById(principal.userId(), requirementId);
    }

    @PutMapping("/api/requirements/{requirementId}")
    public WorkforceRequirementResponse updateRequirement(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID requirementId,
            @Valid @RequestBody UpdateRequirementRequest request) {
        return requirementService.updateRequirement(principal.userId(), requirementId, request);
    }

    @PostMapping("/api/requirements/{requirementId}/open")
    public WorkforceRequirementResponse openRequirement(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID requirementId) {
        return requirementService.openRequirement(principal.userId(), requirementId);
    }

    @PostMapping("/api/requirements/{requirementId}/cancel")
    public WorkforceRequirementResponse cancelRequirement(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID requirementId) {
        return requirementService.cancelRequirement(principal.userId(), requirementId);
    }
}
