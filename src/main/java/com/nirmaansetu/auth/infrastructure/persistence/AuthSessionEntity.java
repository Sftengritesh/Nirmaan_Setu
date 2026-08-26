package com.nirmaansetu.auth.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_session")
public class AuthSessionEntity {
    @Id private UUID id;
    @Column(name = "user_id") private UUID userId;
    @Column(name = "token_hash") private String tokenHash;
    @Column(name = "expires_at") private Instant expiresAt;
    @Column(name = "revoked_at") private Instant revokedAt;

    protected AuthSessionEntity() { }
    public AuthSessionEntity(UUID id, UUID userId, String tokenHash, Instant expiresAt) {
        this.id = id; this.userId = userId; this.tokenHash = tokenHash; this.expiresAt = expiresAt;
    }
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isUsable(Instant now) { return revokedAt == null && expiresAt.isAfter(now); }
    public void revoke(Instant now) { revokedAt = now; }
}
