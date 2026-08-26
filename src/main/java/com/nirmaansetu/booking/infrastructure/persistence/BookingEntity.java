package com.nirmaansetu.booking.infrastructure.persistence;

import com.nirmaansetu.booking.domain.BookingStatus;
import com.nirmaansetu.booking.domain.ProviderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "booking")
public class BookingEntity {

    @Id
    private UUID id;

    @Column(name = "requirement_id", nullable = false)
    private UUID requirementId;

    @Column(name = "provider_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    @Column(name = "provider_worker_profile_id")
    private UUID providerWorkerProfileId;

    @Column(name = "provider_team_id")
    private UUID providerTeamId;

    @Column(name = "provider_contractor_profile_id")
    private UUID providerContractorProfileId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BookingEntity() {}

    public BookingEntity(UUID id, UUID requirementId, ProviderType providerType, Integer quantity,
                         BookingStatus status, Instant now) {
        this.id = id;
        this.requirementId = requirementId;
        this.providerType = providerType;
        this.quantity = quantity;
        this.status = status;
        this.requestedAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getRequirementId() { return requirementId; }
    public ProviderType getProviderType() { return providerType; }
    public UUID getProviderWorkerProfileId() { return providerWorkerProfileId; }
    public void setProviderWorkerProfileId(UUID providerWorkerProfileId) { this.providerWorkerProfileId = providerWorkerProfileId; }
    public UUID getProviderTeamId() { return providerTeamId; }
    public void setProviderTeamId(UUID providerTeamId) { this.providerTeamId = providerTeamId; }
    public UUID getProviderContractorProfileId() { return providerContractorProfileId; }
    public void setProviderContractorProfileId(UUID providerContractorProfileId) { this.providerContractorProfileId = providerContractorProfileId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public Instant getRequestedAt() { return requestedAt; }
    public Instant getRespondedAt() { return respondedAt; }
    public void setRespondedAt(Instant respondedAt) { this.respondedAt = respondedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
