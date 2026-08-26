package com.nirmaansetu.auth.application;

import com.nirmaansetu.auth.domain.AuthRole;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("roleAuthorization")
public class RoleAuthorization {
    public boolean hasAnyRole(Authentication authentication, AuthRole... roles) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) return false;
        for (AuthRole role : roles) if (principal.roles().contains(role)) return true;
        return false;
    }
}
