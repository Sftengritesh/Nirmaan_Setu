package com.nirmaansetu.contractor.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractorWorkerAssociationRepository extends JpaRepository<ContractorWorkerAssociationEntity, ContractorWorkerAssociationId> {
    List<ContractorWorkerAssociationEntity> findByIdContractorProfileId(UUID contractorProfileId);
}
