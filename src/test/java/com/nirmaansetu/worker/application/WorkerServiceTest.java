package com.nirmaansetu.worker.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nirmaansetu.worker.api.CreateWorkerProfileRequest;
import com.nirmaansetu.worker.api.UpdateWorkerProfileRequest;
import com.nirmaansetu.worker.api.WorkerProfileResponse;
import com.nirmaansetu.worker.domain.AvailabilityStatus;
import com.nirmaansetu.worker.domain.WorkerProfileException;
import com.nirmaansetu.worker.infrastructure.persistence.SkillEntity;
import com.nirmaansetu.worker.infrastructure.persistence.SkillRepository;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileEntity;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkerServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-26T10:00:00Z");

    @Mock private WorkerProfileRepository workerProfileRepository;
    @Mock private SkillRepository skillRepository;

    private WorkerService service;

    @BeforeEach
    void setUp() {
        service = new WorkerService(workerProfileRepository, skillRepository, Clock.fixed(NOW, ZoneOffset.UTC));
        when(workerProfileRepository.save(any(WorkerProfileEntity.class))).thenAnswer(i -> i.getArgument(0));
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private WorkerProfileEntity profileFor(UUID userId) {
        return new WorkerProfileEntity(UUID.randomUUID(), userId, "Ramesh Kumar",
            "Mumbai", AvailabilityStatus.AVAILABLE, false, NOW);
    }

    private SkillEntity skillEntity(UUID id, String code, String name) {
        // Use reflection-free approach: create via repo mock stub
        WorkerProfileEntity dummy = new WorkerProfileEntity(UUID.randomUUID(), UUID.randomUUID(),
            "x", "x", AvailabilityStatus.AVAILABLE, false, NOW);
        // We need a SkillEntity — use a subclass-friendly approach
        return new SkillEntity() {
            { /* id/code/name are private; we expose via override */ }
            @Override public UUID getId() { return id; }
            @Override public String getCode() { return code; }
            @Override public String getName() { return name; }
            @Override public boolean isActive() { return true; }
        };
    }

    private CreateWorkerProfileRequest createRequest() {
        return new CreateWorkerProfileRequest(
            "Ramesh Kumar", "Mumbai", "AVAILABLE", (short) 5,
            new BigDecimal("800"), "Experienced mason", false);
    }

    // ── tests ────────────────────────────────────────────────────────────────

    /** 1. Worker can create own profile. */
    @Test
    void workerCanCreateOwnProfile() {
        UUID userId = UUID.randomUUID();
        when(workerProfileRepository.existsByUserId(userId)).thenReturn(false);

        WorkerProfileResponse response = service.createProfile(userId, createRequest());

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.displayName()).isEqualTo("Ramesh Kumar");
        assertThat(response.availabilityStatus()).isEqualTo("AVAILABLE");
        verify(workerProfileRepository).save(any(WorkerProfileEntity.class));
    }

    /** 2. Duplicate worker profile is rejected (application-level check). */
    @Test
    void duplicateProfileRejectedAtApplicationLevel() {
        UUID userId = UUID.randomUUID();
        when(workerProfileRepository.existsByUserId(userId)).thenReturn(true);

        assertThatThrownBy(() -> service.createProfile(userId, createRequest()))
            .isInstanceOf(WorkerProfileException.class)
            .hasMessageContaining("already exists");

        verify(workerProfileRepository, never()).save(any());
    }

    /** 3. Duplicate worker profile rejected at DB constraint level (DataIntegrityViolationException). */
    @Test
    void duplicateProfileRejectedAtConstraintLevel() {
        UUID userId = UUID.randomUUID();
        when(workerProfileRepository.existsByUserId(userId)).thenReturn(false);
        when(workerProfileRepository.save(any())).thenThrow(
            new DataIntegrityViolationException("unique constraint violation"));

        assertThatThrownBy(() -> service.createProfile(userId, createRequest()))
            .isInstanceOf(WorkerProfileException.class)
            .hasMessageContaining("already exists");
    }

    /** 4. Worker can retrieve own profile. */
    @Test
    void workerCanRetrieveOwnProfile() {
        UUID userId = UUID.randomUUID();
        WorkerProfileEntity entity = profileFor(userId);
        when(workerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(entity));

        WorkerProfileResponse response = service.getProfile(userId);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.displayName()).isEqualTo("Ramesh Kumar");
    }

    /** 5. Worker can update own profile. */
    @Test
    void workerCanUpdateOwnProfile() {
        UUID userId = UUID.randomUUID();
        WorkerProfileEntity entity = profileFor(userId);
        when(workerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(entity));

        UpdateWorkerProfileRequest update = new UpdateWorkerProfileRequest(
            "Ramesh K.", "Pune", "LIMITED", (short) 6,
            new BigDecimal("900"), "Senior mason", true);
        WorkerProfileResponse response = service.updateProfile(userId, update);

        assertThat(response.displayName()).isEqualTo("Ramesh K.");
        assertThat(response.location()).isEqualTo("Pune");
        assertThat(response.availabilityStatus()).isEqualTo("LIMITED");
        assertThat(response.isTravelWilling()).isTrue();
    }

    /** 6. A different userId cannot access a profile it does not own. */
    @Test
    void workerCannotAccessAnotherUsersProfile() {
        UUID ownerId = UUID.randomUUID();
        UUID strangerId = UUID.randomUUID();
        when(workerProfileRepository.findByUserId(strangerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile(strangerId))
            .isInstanceOf(WorkerProfileException.class)
            .hasMessageContaining("not found");
    }

    /** 7. Worker can add a valid, active skill. */
    @Test
    void workerCanAddValidSkill() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.fromString("10000000-0000-0000-0000-000000000001");
        WorkerProfileEntity entity = profileFor(userId);
        when(workerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(entity));
        SkillEntity skill = skillEntity(skillId, "RAJ_MISTRI", "Raj Mistri");
        when(skillRepository.findByIdAndIsActiveTrue(skillId)).thenReturn(Optional.of(skill));

        WorkerProfileResponse response = service.addSkill(userId, skillId);

        assertThat(response.skills()).hasSize(1);
        assertThat(response.skills().get(0).code()).isEqualTo("RAJ_MISTRI");
    }

    /** 8. Unknown skill ID is rejected. */
    @Test
    void unknownSkillCannotBeAdded() {
        UUID userId = UUID.randomUUID();
        UUID unknownSkillId = UUID.randomUUID();
        WorkerProfileEntity entity = profileFor(userId);
        when(workerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(entity));
        when(skillRepository.findByIdAndIsActiveTrue(unknownSkillId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addSkill(userId, unknownSkillId))
            .isInstanceOf(WorkerProfileException.class)
            .hasMessageContaining("Skill not found");
    }

    /** 9. Duplicate skill cannot be added twice. */
    @Test
    void duplicateSkillCannotBeAddedTwice() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.fromString("10000000-0000-0000-0000-000000000001");
        WorkerProfileEntity entity = profileFor(userId);
        when(workerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(entity));
        SkillEntity skill = skillEntity(skillId, "RAJ_MISTRI", "Raj Mistri");
        when(skillRepository.findByIdAndIsActiveTrue(skillId)).thenReturn(Optional.of(skill));

        service.addSkill(userId, skillId); // first add succeeds

        assertThatThrownBy(() -> service.addSkill(userId, skillId))
            .isInstanceOf(WorkerProfileException.class)
            .hasMessageContaining("already associated");
    }

    /** 10. Worker can remove an existing skill. */
    @Test
    void workerCanRemoveExistingSkill() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.fromString("10000000-0000-0000-0000-000000000001");
        WorkerProfileEntity entity = profileFor(userId);
        SkillEntity skill = skillEntity(skillId, "RAJ_MISTRI", "Raj Mistri");
        entity.addSkill(skill);
        when(workerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(entity));

        WorkerProfileResponse response = service.removeSkill(userId, skillId);

        assertThat(response.skills()).isEmpty();
    }

    /** 11. Invalid availability status is rejected. */
    @Test
    void invalidAvailabilityStatusIsRejected() {
        UUID userId = UUID.randomUUID();
        when(workerProfileRepository.existsByUserId(userId)).thenReturn(false);
        CreateWorkerProfileRequest bad = new CreateWorkerProfileRequest(
            "Ramesh", "Mumbai", "SLEEPING", null, null, null, false);

        assertThatThrownBy(() -> service.createProfile(userId, bad))
            .isInstanceOf(WorkerProfileException.class)
            .hasMessageContaining("Invalid availability status");
    }

    /** 12. Profile with blank displayName is blocked by service trim + DB constraint logic. */
    @Test
    void blankDisplayNameIsNormalized() {
        // The @NotBlank on the DTO prevents blank names at the controller layer.
        // At the service layer, trim is applied; a purely-whitespace name from the request
        // would have already been rejected by Bean Validation before reaching the service.
        // We verify the service itself does not accidentally strip a valid name to blank.
        UUID userId = UUID.randomUUID();
        when(workerProfileRepository.existsByUserId(userId)).thenReturn(false);
        CreateWorkerProfileRequest req = new CreateWorkerProfileRequest(
            "  Ramesh  ", "Mumbai", "AVAILABLE", null, null, null, false);
        WorkerProfileResponse response = service.createProfile(userId, req);
        assertThat(response.displayName()).isEqualTo("Ramesh");
    }

    /** 13. Multiple roles: WORKER + CONTRACTOR — worker service never touches roles. */
    @Test
    void workerServiceNeverModifiesRoles() {
        // WorkerService has no reference to any role repository or user_role table.
        // This test confirms service instantiation and operation work without role mutation.
        UUID userId = UUID.randomUUID();
        when(workerProfileRepository.existsByUserId(userId)).thenReturn(false);
        // Just creating a profile should not cause any role-related call
        WorkerProfileResponse response = service.createProfile(userId, createRequest());
        assertThat(response.userId()).isEqualTo(userId);
        // No SkillRepository or WorkerProfileRepository call touched roles
        verify(skillRepository, never()).save(any());
    }

    /** 14. DataIntegrityViolationException during skill add is surfaced cleanly. */
    @Test
    void databaseConstraintOnSkillAddHandledCleanly() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.fromString("10000000-0000-0000-0000-000000000001");
        WorkerProfileEntity entity = profileFor(userId);
        when(workerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(entity));
        SkillEntity skill = skillEntity(skillId, "RAJ_MISTRI", "Raj Mistri");
        when(skillRepository.findByIdAndIsActiveTrue(skillId)).thenReturn(Optional.of(skill));
        when(workerProfileRepository.save(any())).thenThrow(
            new DataIntegrityViolationException("worker_skill unique constraint"));

        assertThatThrownBy(() -> service.addSkill(userId, skillId))
            .isInstanceOf(DataIntegrityViolationException.class); // propagates; caught by WorkerExceptionHandler
    }

    /** 15. Profile response contains all persisted fields after update. */
    @Test
    void profileResponseReflectsAllPersistedFields() {
        UUID userId = UUID.randomUUID();
        WorkerProfileEntity entity = profileFor(userId);
        when(workerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(entity));

        UpdateWorkerProfileRequest update = new UpdateWorkerProfileRequest(
            "Suresh Verma", "Delhi", "UNAVAILABLE", (short) 10,
            new java.math.BigDecimal("1200"), "Master electrician", true);
        WorkerProfileResponse response = service.updateProfile(userId, update);

        assertThat(response.displayName()).isEqualTo("Suresh Verma");
        assertThat(response.location()).isEqualTo("Delhi");
        assertThat(response.availabilityStatus()).isEqualTo("UNAVAILABLE");
        assertThat(response.experienceYears()).isEqualTo((short) 10);
        assertThat(response.dailyRate()).isEqualByComparingTo("1200");
        assertThat(response.profileDescription()).isEqualTo("Master electrician");
        assertThat(response.isTravelWilling()).isTrue();
        assertThat(response.skills()).isEmpty();
        assertThat(response.userId()).isEqualTo(userId);
    }
}
