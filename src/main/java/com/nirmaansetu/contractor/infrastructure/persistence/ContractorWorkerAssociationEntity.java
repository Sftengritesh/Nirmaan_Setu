package com.nirmaansetu.contractor.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "contractor_worker_association")
public class ContractorWorkerAssociationEntity {

    @EmbeddedId
    private ContractorWorkerAssociationId id;

    @Column(name = "ends_on")
    private LocalDate endsOn;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ContractorWorkerAssociationEntity() {}

    public ContractorWorkerAssociationEntity(ContractorWorkerAssociationId id, LocalDate endsOn, Instant createdAt) {
        this.id = id;
        this.endsOn = endsOn;
        this.createdAt = createdAt;
    }

    public ContractorWorkerAssociationId getId() { return id; }
    public LocalDate getEndsOn() { return endsOn; }
    public void setEndsOn(LocalDate endsOn) { this.endsOn = endsOn; }
    public Instant getCreatedAt() { return createdAt; }
}
