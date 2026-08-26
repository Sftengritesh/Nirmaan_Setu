package com.nirmaansetu.client.api;

import com.nirmaansetu.auth.application.AuthPrincipal;
import com.nirmaansetu.client.application.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
@PreAuthorize("hasRole('CLIENT')")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping("/profile")
    public ResponseEntity<ClientProfileResponse> createProfile(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateClientProfileRequest request) {
        ClientProfileResponse response = clientService.createProfile(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/profile/me")
    public ClientProfileResponse getProfile(@AuthenticationPrincipal AuthPrincipal principal) {
        return clientService.getProfile(principal.userId());
    }

    @PutMapping("/profile/me")
    public ClientProfileResponse updateProfile(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody UpdateClientProfileRequest request) {
        return clientService.updateProfile(principal.userId(), request);
    }
}
