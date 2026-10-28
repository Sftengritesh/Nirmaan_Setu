package com.nirmaansetu.operations.dpr;

import com.nirmaansetu.auth.application.AuthPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dpr")
public class DprController {

    private final DprService dprService;

    public DprController(DprService dprService) {
        this.dprService = dprService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTRACTOR')")
    public ResponseEntity<DprResponse> createDpr(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateDprRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dprService.createDpr(principal.userId(), request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTRACTOR', 'CLIENT')")
    public List<DprResponse> getDprs(@RequestParam(required = false) UUID projectId) {
        return dprService.getDprs(projectId);
    }
}
