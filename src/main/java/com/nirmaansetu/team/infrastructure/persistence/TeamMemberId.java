package com.nirmaansetu.team.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class TeamMemberId implements Serializable {

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(name = "worker_profile_id", nullable = false)
    private UUID workerProfileId;

    @Column(name = "starts_on", nullable = false)
    private LocalDate startsOn;

    public TeamMemberId() {}

    public TeamMemberId(UUID teamId, UUID workerProfileId, LocalDate startsOn) {
        this.teamId = teamId;
        this.workerProfileId = workerProfileId;
        this.startsOn = startsOn;
    }

    public UUID getTeamId() { return teamId; }
    public UUID getWorkerProfileId() { return workerProfileId; }
    public LocalDate getStartsOn() { return startsOn; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamMemberId that = (TeamMemberId) o;
        return Objects.equals(teamId, that.teamId) &&
               Objects.equals(workerProfileId, that.workerProfileId) &&
               Objects.equals(startsOn, that.startsOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, workerProfileId, startsOn);
    }
}
