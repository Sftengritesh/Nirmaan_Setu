package com.nirmaansetu.auth.api;

import com.nirmaansetu.auth.application.AuthPrincipal;
import com.nirmaansetu.auth.application.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/otp/start")
    public ResponseEntity<Void> startOtp(@Valid @RequestBody StartOtpRequest request) {
        authService.startOtp(request.phoneNumber());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/otp/verify")
    public AuthResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return AuthResponse.from(authService.verifyOtp(request.phoneNumber(), request.otp()));
    }

    @GetMapping("/me")
    public CurrentUserResponse currentUser(@AuthenticationPrincipal AuthPrincipal principal) {
        return CurrentUserResponse.from(principal);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AuthPrincipal principal) {
        authService.logout(principal);
        return ResponseEntity.noContent().build();
    }
}
