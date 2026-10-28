package com.nirmaansetu.operations.milestone;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/milestones")
public class MilestoneController {

    private final MilestoneService milestoneService;

    public MilestoneController(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    @GetMapping("/project/{projectId}")
    public List<MilestoneResponse> getProjectMilestones(@PathVariable UUID projectId) {
        return milestoneService.getMilestonesByProject(projectId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'CONTRACTOR')")
    public ResponseEntity<MilestoneResponse> createMilestone(@Valid @RequestBody CreateMilestoneRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(milestoneService.createMilestone(req));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTRACTOR')")
    public MilestoneResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateMilestoneStatusRequest req) {
        return milestoneService.updateMilestoneStatus(id, req.status());
    }
}
