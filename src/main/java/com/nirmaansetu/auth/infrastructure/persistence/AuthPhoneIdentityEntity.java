package com.nirmaansetu.auth.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_phone_identity")
public class AuthPhoneIdentityEntity {
    @Id private UUID id;
    @Column(name = "user_id") private UUID userId;
    @Column(name = "phone_e164") private String phoneE164;
    @Column(name = "verified_at") private Instant verifiedAt;

    protected AuthPhoneIdentityEntity() { }
    public AuthPhoneIdentityEntity(UUID id, UUID userId, String phoneE164, Instant verifiedAt) {
        this.id = id; this.userId = userId; this.phoneE164 = phoneE164; this.verifiedAt = verifiedAt;
    }
    public UUID getUserId() { return userId; }
    public String getPhoneE164() { return phoneE164; }
}
