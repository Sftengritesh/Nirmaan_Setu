package com.nirmaansetu.auth.api;

import com.nirmaansetu.auth.application.AuthSessionResult;
import com.nirmaansetu.auth.domain.AuthRole;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AuthResponse(UUID userId, Set<AuthRole> roles, String accessToken, Instant expiresAt) {
    static AuthResponse from(AuthSessionResult result) { return new AuthResponse(result.userId(), result.roles(), result.accessToken(), result.expiresAt()); }
}
