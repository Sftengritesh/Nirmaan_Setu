package com.nirmaansetu.verification.api;

import com.nirmaansetu.auth.application.AuthPrincipal;
import com.nirmaansetu.verification.application.VerificationService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubjectVerificationController {

    private final VerificationService verificationService;

    public SubjectVerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/api/verifications/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VerificationResponse> submitUserVerification(
            @AuthenticationPrincipal AuthPrincipal principal) {
        VerificationResponse response = verificationService.submitUserVerification(principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/api/verifications/worker")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<VerificationResponse> submitWorkerVerification(
            @AuthenticationPrincipal AuthPrincipal principal) {
        VerificationResponse response = verificationService.submitWorkerVerification(principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/api/verifications/contractor")
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<VerificationResponse> submitContractorVerification(
            @AuthenticationPrincipal AuthPrincipal principal) {
        VerificationResponse response = verificationService.submitContractorVerification(principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/api/verifications/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<VerificationResponse> submitClientVerification(
            @AuthenticationPrincipal AuthPrincipal principal) {
        VerificationResponse response = verificationService.submitClientVerification(principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/verifications/me")
    @PreAuthorize("isAuthenticated()")
    public List<VerificationResponse> getMyVerifications(
            @AuthenticationPrincipal AuthPrincipal principal) {
        return verificationService.getMyVerifications(principal.userId());
    }
}
