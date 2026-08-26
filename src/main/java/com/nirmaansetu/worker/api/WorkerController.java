package com.nirmaansetu.worker.api;

import com.nirmaansetu.worker.application.WorkerService;
import com.nirmaansetu.worker.domain.WorkerProfileException;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nirmaansetu.auth.application.AuthPrincipal;

@RestController
@RequestMapping("/api/workers")
@PreAuthorize("hasRole('WORKER')")
public class WorkerController {

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @PostMapping
    public ResponseEntity<WorkerProfileResponse> createProfile(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateWorkerProfileRequest request) {
        WorkerProfileResponse response = workerService.createProfile(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public WorkerProfileResponse getProfile(@AuthenticationPrincipal AuthPrincipal principal) {
        return workerService.getProfile(principal.userId());
    }

    @PutMapping("/me")
    public WorkerProfileResponse updateProfile(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody UpdateWorkerProfileRequest request) {
        return workerService.updateProfile(principal.userId(), request);
    }

    @PostMapping("/me/skills")
    public WorkerProfileResponse addSkill(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody AddSkillRequest request) {
        return workerService.addSkill(principal.userId(), request.skillId());
    }

    @DeleteMapping("/me/skills/{skillId}")
    public WorkerProfileResponse removeSkill(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID skillId) {
        return workerService.removeSkill(principal.userId(), skillId);
    }
}
