package com.nirmaansetu.contractor.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nirmaansetu.contractor.api.AssociateWorkerRequest;
import com.nirmaansetu.contractor.api.ContractorProfileResponse;
import com.nirmaansetu.contractor.api.ContractorWorkerResponse;
import com.nirmaansetu.contractor.api.CreateContractorProfileRequest;
import com.nirmaansetu.contractor.api.UpdateContractorProfileRequest;
import com.nirmaansetu.contractor.domain.ContractorProfileException;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorProfileEntity;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorProfileRepository;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorWorkerAssociationEntity;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorWorkerAssociationId;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorWorkerAssociationRepository;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
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
class ContractorServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-26T10:00:00Z");

    @Mock private ContractorProfileRepository contractorProfileRepository;
    @Mock private ContractorWorkerAssociationRepository associationRepository;
    @Mock private WorkerProfileRepository workerProfileRepository;

    private ContractorService service;

    @BeforeEach
    void setUp() {
        service = new ContractorService(
            contractorProfileRepository, associationRepository, workerProfileRepository, Clock.fixed(NOW, ZoneOffset.UTC)
        );
        when(contractorProfileRepository.save(any(ContractorProfileEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(associationRepository.save(any(ContractorWorkerAssociationEntity.class))).thenAnswer(i -> i.getArgument(0));
    }

    private ContractorProfileEntity profileFor(UUID userId) {
        ContractorProfileEntity entity = new ContractorProfileEntity(
            UUID.randomUUID(), userId, "Apex Infrastructure", "Bangalore", NOW
        );
        entity.setDescription("Commercial & Residential Contractor");
        return entity;
    }

    private CreateContractorProfileRequest createRequest() {
        return new CreateContractorProfileRequest("Apex Infrastructure", "Bangalore", "Commercial & Residential Contractor");
    }

    @Test
    void contractorCanCreateProfile() {
        UUID userId = UUID.randomUUID();
        when(contractorProfileRepository.existsByUserId(userId)).thenReturn(false);

        ContractorProfileResponse response = service.createProfile(userId, createRequest());

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.displayName()).isEqualTo("Apex Infrastructure");
        assertThat(response.location()).isEqualTo("Bangalore");
        verify(contractorProfileRepository).save(any(ContractorProfileEntity.class));
    }

    @Test
    void duplicateProfileRejectedAtApplicationLevel() {
        UUID userId = UUID.randomUUID();
        when(contractorProfileRepository.existsByUserId(userId)).thenReturn(true);

        assertThatThrownBy(() -> service.createProfile(userId, createRequest()))
            .isInstanceOf(ContractorProfileException.class)
            .hasMessageContaining("already exists");

        verify(contractorProfileRepository, never()).save(any());
    }

    @Test
    void duplicateProfileRejectedAtConstraintLevel() {
        UUID userId = UUID.randomUUID();
        when(contractorProfileRepository.existsByUserId(userId)).thenReturn(false);
        when(contractorProfileRepository.save(any())).thenThrow(
            new DataIntegrityViolationException("unique constraint violation")
        );

        assertThatThrownBy(() -> service.createProfile(userId, createRequest()))
            .isInstanceOf(ContractorProfileException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    void contractorCanRetrieveProfile() {
        UUID userId = UUID.randomUUID();
        ContractorProfileEntity entity = profileFor(userId);
        when(contractorProfileRepository.findByUserId(userId)).thenReturn(Optional.of(entity));

        ContractorProfileResponse response = service.getProfile(userId);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.displayName()).isEqualTo("Apex Infrastructure");
    }

    @Test
    void contractorCanUpdateProfile() {
        UUID userId = UUID.randomUUID();
        ContractorProfileEntity entity = profileFor(userId);
        when(contractorProfileRepository.findByUserId(userId)).thenReturn(Optional.of(entity));

        UpdateContractorProfileRequest update = new UpdateContractorProfileRequest(
            "Apex Infra Projects", "Hyderabad", "Updated Description"
        );
        ContractorProfileResponse response = service.updateProfile(userId, update);

        assertThat(response.displayName()).isEqualTo("Apex Infra Projects");
        assertThat(response.location()).isEqualTo("Hyderabad");
        assertThat(response.description()).isEqualTo("Updated Description");
    }

    @Test
    void unknownUserProfileAccessFails() {
        UUID strangerId = UUID.randomUUID();
        when(contractorProfileRepository.findByUserId(strangerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile(strangerId))
            .isInstanceOf(ContractorProfileException.class)
            .hasMessageContaining("not found");
    }

    @Test
    void associateWorkerSuccess() {
        UUID userId = UUID.randomUUID();
        UUID workerProfileId = UUID.randomUUID();
        ContractorProfileEntity profile = profileFor(userId);

        when(contractorProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(workerProfileRepository.existsById(workerProfileId)).thenReturn(true);

        AssociateWorkerRequest request = new AssociateWorkerRequest(
            workerProfileId, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 31)
        );

        ContractorWorkerResponse response = service.associateWorker(userId, request);

        assertThat(response.contractorProfileId()).isEqualTo(profile.getId());
        assertThat(response.workerProfileId()).isEqualTo(workerProfileId);
        assertThat(response.startsOn()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(response.endsOn()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void associateWorkerFailsIfWorkerNotFound() {
        UUID userId = UUID.randomUUID();
        UUID workerProfileId = UUID.randomUUID();
        ContractorProfileEntity profile = profileFor(userId);

        when(contractorProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(workerProfileRepository.existsById(workerProfileId)).thenReturn(false);

        AssociateWorkerRequest request = new AssociateWorkerRequest(
            workerProfileId, LocalDate.of(2026, 9, 1), null
        );

        assertThatThrownBy(() -> service.associateWorker(userId, request))
            .isInstanceOf(ContractorProfileException.class)
            .hasMessageContaining("Worker profile not found");
    }

    @Test
    void associateWorkerFailsIfEndBeforeStart() {
        UUID userId = UUID.randomUUID();
        UUID workerProfileId = UUID.randomUUID();
        ContractorProfileEntity profile = profileFor(userId);

        when(contractorProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(workerProfileRepository.existsById(workerProfileId)).thenReturn(true);

        AssociateWorkerRequest request = new AssociateWorkerRequest(
            workerProfileId, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 8, 1)
        );

        assertThatThrownBy(() -> service.associateWorker(userId, request))
            .isInstanceOf(ContractorProfileException.class)
            .hasMessageContaining("End date cannot be before start date");
    }

    @Test
    void endWorkerAssociationSuccess() {
        UUID userId = UUID.randomUUID();
        UUID workerProfileId = UUID.randomUUID();
        ContractorProfileEntity profile = profileFor(userId);
        LocalDate startsOn = LocalDate.of(2026, 9, 1);

        when(contractorProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        ContractorWorkerAssociationId id = new ContractorWorkerAssociationId(profile.getId(), workerProfileId, startsOn);
        ContractorWorkerAssociationEntity assoc = new ContractorWorkerAssociationEntity(id, null, NOW);

        when(associationRepository.findById(id)).thenReturn(Optional.of(assoc));

        LocalDate endsOn = LocalDate.of(2026, 10, 15);
        ContractorWorkerResponse response = service.endWorkerAssociation(userId, workerProfileId, startsOn, endsOn);

        assertThat(response.endsOn()).isEqualTo(endsOn);
        verify(associationRepository, never()).delete(any());
    }

    @Test
    void anotherContractorCannotEndWorkerAssociation() {
        UUID contractorAUserId = UUID.randomUUID();
        UUID contractorBUserId = UUID.randomUUID();
        UUID workerProfileId = UUID.randomUUID();
        ContractorProfileEntity profileB = profileFor(contractorBUserId);
        LocalDate startsOn = LocalDate.of(2026, 9, 1);

        when(contractorProfileRepository.findByUserId(contractorBUserId)).thenReturn(Optional.of(profileB));

        // Association belongs to Contractor A (different profile ID)
        ContractorWorkerAssociationId idForB = new ContractorWorkerAssociationId(profileB.getId(), workerProfileId, startsOn);
        when(associationRepository.findById(idForB)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.endWorkerAssociation(contractorBUserId, workerProfileId, startsOn, LocalDate.of(2026, 10, 1)))
            .isInstanceOf(ContractorProfileException.class)
            .hasMessageContaining("association not found");
    }

    @Test
    void getAssociatedWorkersReturnsList() {
        UUID userId = UUID.randomUUID();
        UUID workerProfileId = UUID.randomUUID();
        ContractorProfileEntity profile = profileFor(userId);

        when(contractorProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        ContractorWorkerAssociationId id = new ContractorWorkerAssociationId(profile.getId(), workerProfileId, LocalDate.of(2026, 9, 1));
        ContractorWorkerAssociationEntity assoc = new ContractorWorkerAssociationEntity(id, null, NOW);

        when(associationRepository.findByIdContractorProfileId(profile.getId())).thenReturn(List.of(assoc));

        List<ContractorWorkerResponse> list = service.getAssociatedWorkers(userId);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).workerProfileId()).isEqualTo(workerProfileId);
    }
}
