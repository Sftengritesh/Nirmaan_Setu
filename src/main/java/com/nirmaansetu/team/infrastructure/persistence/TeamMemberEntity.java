package com.nirmaansetu.team.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "team_member")
public class TeamMemberEntity {

    @EmbeddedId
    private TeamMemberId id;

    @Column(name = "ends_on")
    private LocalDate endsOn;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TeamMemberEntity() {}

    public TeamMemberEntity(TeamMemberId id, LocalDate endsOn, Instant createdAt) {
        this.id = id;
        this.endsOn = endsOn;
        this.createdAt = createdAt;
    }

    public TeamMemberId getId() { return id; }
    public LocalDate getEndsOn() { return endsOn; }
    public void setEndsOn(LocalDate endsOn) { this.endsOn = endsOn; }
    public Instant getCreatedAt() { return createdAt; }
}
