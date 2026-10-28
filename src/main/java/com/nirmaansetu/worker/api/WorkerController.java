package com.nirmaansetu.worker.api;

import com.nirmaansetu.auth.application.AuthPrincipal;
import com.nirmaansetu.worker.application.WorkerService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @PostMapping
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<WorkerProfileResponse> createProfile(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateWorkerProfileRequest request) {
        WorkerProfileResponse response = workerService.createProfile(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('WORKER')")
    public WorkerProfileResponse getProfile(@AuthenticationPrincipal AuthPrincipal principal) {
        return workerService.getProfile(principal.userId());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTRACTOR')")
    public List<WorkerProfileResponse> getAllWorkers() {
        return workerService.listAllWorkers();
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('WORKER')")
    public WorkerProfileResponse updateProfile(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody UpdateWorkerProfileRequest request) {
        return workerService.updateProfile(principal.userId(), request);
    }

    @PostMapping("/me/skills")
    @PreAuthorize("hasRole('WORKER')")
    public WorkerProfileResponse addSkill(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody AddSkillRequest request) {
        return workerService.addSkill(principal.userId(), request.skillId());
    }

    @DeleteMapping("/me/skills/{skillId}")
    @PreAuthorize("hasRole('WORKER')")
    public WorkerProfileResponse removeSkill(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID skillId) {
        return workerService.removeSkill(principal.userId(), skillId);
    }
}
