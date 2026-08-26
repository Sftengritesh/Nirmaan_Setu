package com.nirmaansetu.team.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMemberEntity, TeamMemberId> {
    List<TeamMemberEntity> findByIdTeamId(UUID teamId);
}
