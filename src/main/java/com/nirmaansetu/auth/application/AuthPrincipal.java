package com.nirmaansetu.auth.application;

import com.nirmaansetu.auth.domain.AuthRole;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record AuthPrincipal(UUID userId, UUID sessionId, Set<AuthRole> roles) implements UserDetails {
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).toList();
    }
    @Override public String getPassword() { return ""; }
    @Override public String getUsername() { return userId.toString(); }
}
