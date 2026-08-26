package com.nirmaansetu.auth.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_otp_challenge")
public class AuthOtpChallengeEntity {
    @Id private UUID id;
    @Column(name = "phone_e164") private String phoneE164;
    @Column(name = "code_hash") private String codeHash;
    @Column(name = "expires_at") private Instant expiresAt;
    @Column(name = "consumed_at") private Instant consumedAt;
    private short attempts;
    @Column(name = "created_at") private Instant createdAt;

    protected AuthOtpChallengeEntity() { }
    public AuthOtpChallengeEntity(UUID id, String phoneE164, String codeHash, Instant expiresAt, Instant createdAt) {
        this.id = id; this.phoneE164 = phoneE164; this.codeHash = codeHash; this.expiresAt = expiresAt; this.createdAt = createdAt;
    }
    public boolean isExpired(Instant now) { return !expiresAt.isAfter(now); }
    public boolean isConsumed() { return consumedAt != null; }
    public UUID getId() { return id; }
    public boolean matches(String code, org.springframework.security.crypto.password.PasswordEncoder encoder) { return encoder.matches(code, codeHash); }
    public int getAttempts() { return attempts; }
    public void registerFailedAttempt() { attempts++; }
    public void consume(Instant now) { consumedAt = now; }
}
