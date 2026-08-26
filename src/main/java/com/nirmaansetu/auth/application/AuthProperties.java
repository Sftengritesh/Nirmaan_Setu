package com.nirmaansetu.auth.application;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    private final Otp otp = new Otp();
    private final Session session = new Session();

    public Otp getOtp() { return otp; }
    public Session getSession() { return session; }

    public static class Otp {
        @NotNull private Duration ttl = Duration.ofMinutes(10);
        @NotNull private Duration retention = Duration.ofDays(1);
        private int maxAttempts = 5;
        public Duration getTtl() { return ttl; }
        public void setTtl(Duration ttl) { this.ttl = ttl; }
        public Duration getRetention() { return retention; }
        public void setRetention(Duration retention) { this.retention = retention; }
        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    }

    public static class Session {
        @NotNull private Duration ttl = Duration.ofHours(12);
        public Duration getTtl() { return ttl; }
        public void setTtl(Duration ttl) { this.ttl = ttl; }
    }
}
