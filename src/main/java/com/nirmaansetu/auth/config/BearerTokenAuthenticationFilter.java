package com.nirmaansetu.auth.config;

import com.nirmaansetu.auth.application.AuthPrincipal;
import com.nirmaansetu.auth.application.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {
    private final AuthService authService;
    public BearerTokenAuthenticationFilter(AuthService authService) { this.authService = authService; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            AuthPrincipal principal = authService.authenticate(header.substring(7));
            if (principal != null) SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        }
        chain.doFilter(request, response);
    }
}
