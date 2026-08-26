package com.nirmaansetu.auth.infrastructure.persistence;

import com.nirmaansetu.auth.domain.AccountStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class AppUserEntity {
    @Id private UUID id;
    @Column(name = "account_status") private String accountStatus;
    @Column(name = "created_at") private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;

    protected AppUserEntity() { }
    public AppUserEntity(UUID id) { this.id = id; this.accountStatus = AccountStatus.ACTIVE.name(); }
    public AppUserEntity(UUID id, AccountStatus accountStatus) { this.id = id; this.accountStatus = accountStatus.name(); }
    public UUID getId() { return id; }
    public AccountStatus getAccountStatus() { return AccountStatus.valueOf(accountStatus); }
}
