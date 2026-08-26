package com.nirmaansetu.contractor.api;

import com.nirmaansetu.auth.application.AuthPrincipal;
import com.nirmaansetu.contractor.application.ContractorService;
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
@RequestMapping("/api/contractors")
@PreAuthorize("hasRole('CONTRACTOR')")
public class ContractorController {

    private final ContractorService contractorService;

    public ContractorController(ContractorService contractorService) {
        this.contractorService = contractorService;
    }

    @PostMapping("/profile")
    public ResponseEntity<ContractorProfileResponse> createProfile(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateContractorProfileRequest request) {
        ContractorProfileResponse response = contractorService.createProfile(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/profile/me")
    public ContractorProfileResponse getProfile(@AuthenticationPrincipal AuthPrincipal principal) {
        return contractorService.getProfile(principal.userId());
    }

    @PutMapping("/profile/me")
    public ContractorProfileResponse updateProfile(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody UpdateContractorProfileRequest request) {
        return contractorService.updateProfile(principal.userId(), request);
    }

    @PostMapping("/workers")
    public ResponseEntity<ContractorWorkerResponse> associateWorker(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody AssociateWorkerRequest request) {
        ContractorWorkerResponse response = contractorService.associateWorker(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/workers")
    public List<ContractorWorkerResponse> getAssociatedWorkers(@AuthenticationPrincipal AuthPrincipal principal) {
        return contractorService.getAssociatedWorkers(principal.userId());
    }

    @PostMapping("/workers/{workerProfileId}/end")
    public ContractorWorkerResponse endWorkerAssociation(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID workerProfileId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startsOn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endsOn) {
        return contractorService.endWorkerAssociation(principal.userId(), workerProfileId, startsOn, endsOn);
    }
}
