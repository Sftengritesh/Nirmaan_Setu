package com.nirmaansetu.verification.api;

import com.nirmaansetu.auth.application.AuthPrincipal;
import com.nirmaansetu.verification.application.VerificationService;
import com.nirmaansetu.verification.domain.SubjectType;
import com.nirmaansetu.verification.domain.VerificationStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminVerificationController {

    private final VerificationService verificationService;

    public AdminVerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping("/api/admin/verifications")
    @PreAuthorize("hasRole('ADMIN')")
    public List<VerificationResponse> listVerifications(
            @RequestParam(required = false) SubjectType subjectType,
            @RequestParam(required = false) VerificationStatus status) {
        return verificationService.listVerificationsForAdmin(subjectType, status);
    }

    @GetMapping("/api/admin/verifications/{verificationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public VerificationResponse getVerification(
            @PathVariable UUID verificationId) {
        return verificationService.getVerificationByIdForAdmin(verificationId);
    }

    @PostMapping("/api/admin/verifications/{verificationId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public VerificationResponse verifyVerification(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID verificationId,
            @RequestBody(required = false) ReviewVerificationRequest request) {
        String notes = request != null ? request.notes() : null;
        return verificationService.verifyVerification(principal.userId(), verificationId, notes);
    }

    @PostMapping("/api/admin/verifications/{verificationId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public VerificationResponse rejectVerification(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID verificationId,
            @RequestBody(required = false) ReviewVerificationRequest request) {
        String notes = request != null ? request.notes() : null;
        return verificationService.rejectVerification(principal.userId(), verificationId, notes);
    }
}
