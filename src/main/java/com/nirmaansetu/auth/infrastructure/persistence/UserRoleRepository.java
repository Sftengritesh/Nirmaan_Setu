package com.nirmaansetu.auth.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserRoleRepository extends Repository<AppUserEntity, UUID> {
    @Query(value = "select r.code from user_role ur join app_role r on r.id = ur.role_id where ur.user_id = :userId", nativeQuery = true)
    List<String> findRoleCodesByUserId(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_role (user_id, role_id, assigned_at) SELECT :userId, id, CURRENT_TIMESTAMP FROM app_role WHERE code = :roleCode ON CONFLICT (user_id, role_id) DO NOTHING", nativeQuery = true)
    int assignRole(@Param("userId") UUID userId, @Param("roleCode") String roleCode);
}
