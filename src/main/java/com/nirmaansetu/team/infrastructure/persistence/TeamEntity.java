package com.nirmaansetu.team.infrastructure.persistence;

import com.nirmaansetu.team.domain.TeamStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "team")
public class TeamEntity {

    @Id
    private UUID id;

    @Column(name = "manager_user_id", nullable = false)
    private UUID managerUserId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TeamStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TeamEntity() {}

    public TeamEntity(UUID id, UUID managerUserId, String name, TeamStatus status, Instant now) {
        this.id = id;
        this.managerUserId = managerUserId;
        this.name = name;
        this.status = status;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getManagerUserId() { return managerUserId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TeamStatus getStatus() { return status; }
    public void setStatus(TeamStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
