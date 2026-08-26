package com.nirmaansetu.auth.config;

import com.nirmaansetu.auth.application.AuthService;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class AuthSecurityConfig {
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public java.time.Clock clock() { return java.time.Clock.systemUTC(); }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthService authService) throws Exception {
        return http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(requests -> requests.requestMatchers("/api/auth/otp/start", "/api/auth/otp/verify", "/error").permitAll().anyRequest().authenticated())
            .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, exception) -> writeError(response, 401, "AUTHENTICATION_REQUIRED", "Authentication is required."))
                .accessDeniedHandler((request, response, exception) -> writeError(response, 403, "ACCESS_DENIED", "Access is denied.")))
            .addFilterBefore(new BearerTokenAuthenticationFilter(authService), UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    private void writeError(jakarta.servlet.http.HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status); response.setContentType("application/json");
        response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
    }
}
