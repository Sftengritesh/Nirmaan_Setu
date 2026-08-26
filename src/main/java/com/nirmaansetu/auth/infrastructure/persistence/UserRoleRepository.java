package com.nirmaansetu.auth.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface UserRoleRepository extends Repository<AppUserEntity, UUID> {
    @Query(value = "select r.code from user_role ur join app_role r on r.id = ur.role_id where ur.user_id = :userId", nativeQuery = true)
    List<String> findRoleCodesByUserId(@Param("userId") UUID userId);
}
