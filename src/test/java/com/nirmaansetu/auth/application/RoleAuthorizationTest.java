package com.nirmaansetu.auth.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.nirmaansetu.auth.domain.AuthRole;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

class RoleAuthorizationTest {
    private final RoleAuthorization authorization = new RoleAuthorization();

    @Test
    void rejectsRolesThatAreNotAssignedAndAcceptsAnyAssignedRole() {
        AuthPrincipal principal = new AuthPrincipal(UUID.randomUUID(), UUID.randomUUID(), Set.of(AuthRole.WORKER, AuthRole.CONTRACTOR));
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        assertThat(authorization.hasAnyRole(authentication, AuthRole.CLIENT)).isFalse();
        assertThat(authorization.hasAnyRole(authentication, AuthRole.CLIENT, AuthRole.CONTRACTOR)).isTrue();
    }
}
