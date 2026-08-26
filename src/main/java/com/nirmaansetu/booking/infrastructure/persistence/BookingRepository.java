package com.nirmaansetu.booking.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {

    List<BookingEntity> findByRequirementId(UUID requirementId);

    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM BookingEntity b WHERE b.requirementId = :requirementId AND b.status = com.nirmaansetu.booking.domain.BookingStatus.ACCEPTED")
    int sumAcceptedQuantityByRequirementId(@Param("requirementId") UUID requirementId);

    List<BookingEntity> findByProviderWorkerProfileId(UUID providerWorkerProfileId);

    List<BookingEntity> findByProviderTeamIdIn(List<UUID> providerTeamIds);

    List<BookingEntity> findByProviderContractorProfileId(UUID providerContractorProfileId);
}
