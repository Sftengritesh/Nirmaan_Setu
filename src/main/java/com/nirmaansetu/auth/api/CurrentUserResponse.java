package com.nirmaansetu.auth.api;

import com.nirmaansetu.auth.application.AuthPrincipal;
import com.nirmaansetu.auth.domain.AuthRole;
import java.util.Set;
import java.util.UUID;

public record CurrentUserResponse(UUID userId, Set<AuthRole> roles) {
    static CurrentUserResponse from(AuthPrincipal principal) { return new CurrentUserResponse(principal.userId(), principal.roles()); }
}
