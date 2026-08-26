package com.nirmaansetu.team.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, UUID> {
    List<TeamEntity> findByManagerUserId(UUID managerUserId);
}
