package com.nirmaansetu.contractor.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ContractorWorkerAssociationId implements Serializable {

    @Column(name = "contractor_profile_id", nullable = false)
    private UUID contractorProfileId;

    @Column(name = "worker_profile_id", nullable = false)
    private UUID workerProfileId;

    @Column(name = "starts_on", nullable = false)
    private LocalDate startsOn;

    public ContractorWorkerAssociationId() {}

    public ContractorWorkerAssociationId(UUID contractorProfileId, UUID workerProfileId, LocalDate startsOn) {
        this.contractorProfileId = contractorProfileId;
        this.workerProfileId = workerProfileId;
        this.startsOn = startsOn;
    }

    public UUID getContractorProfileId() { return contractorProfileId; }
    public UUID getWorkerProfileId() { return workerProfileId; }
    public LocalDate getStartsOn() { return startsOn; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContractorWorkerAssociationId that = (ContractorWorkerAssociationId) o;
        return Objects.equals(contractorProfileId, that.contractorProfileId) &&
               Objects.equals(workerProfileId, that.workerProfileId) &&
               Objects.equals(startsOn, that.startsOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractorProfileId, workerProfileId, startsOn);
    }
}
