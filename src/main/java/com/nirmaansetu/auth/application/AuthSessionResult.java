package com.nirmaansetu.auth.application;

import com.nirmaansetu.auth.domain.AuthRole;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AuthSessionResult(UUID userId, Set<AuthRole> roles, String accessToken, Instant expiresAt) { }
