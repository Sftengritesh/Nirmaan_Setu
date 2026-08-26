package com.nirmaansetu.verification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nirmaansetu.client.domain.ClientType;
import com.nirmaansetu.client.infrastructure.persistence.ClientProfileEntity;
import com.nirmaansetu.client.infrastructure.persistence.ClientProfileRepository;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorProfileEntity;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorProfileRepository;
import com.nirmaansetu.verification.api.VerificationResponse;
import com.nirmaansetu.verification.domain.SubjectType;
import com.nirmaansetu.verification.domain.VerificationException;
import com.nirmaansetu.verification.domain.VerificationStatus;
import com.nirmaansetu.verification.infrastructure.persistence.VerificationEntity;
import com.nirmaansetu.verification.infrastructure.persistence.VerificationRepository;
import com.nirmaansetu.worker.domain.AvailabilityStatus;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileEntity;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VerificationServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-27T00:00:00Z");

    @Mock private VerificationRepository verificationRepository;
    @Mock private WorkerProfileRepository workerProfileRepository;
    @Mock private ContractorProfileRepository contractorProfileRepository;
    @Mock private ClientProfileRepository clientProfileRepository;

    private Clock clock;
    private VerificationService verificationService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(NOW, ZoneOffset.UTC);
        verificationService = new VerificationService(
                verificationRepository,
                workerProfileRepository,
                contractorProfileRepository,
                clientProfileRepository,
                clock
        );

        when(verificationRepository.save(any(VerificationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void submitUserVerificationSuccess() {
        UUID userId = UUID.randomUUID();

        when(verificationRepository.findFirstBySubjectUserIdAndStatus(userId, VerificationStatus.PENDING))
                .thenReturn(Optional.empty());
        when(verificationRepository.findFirstBySubjectUserIdAndStatus(userId, VerificationStatus.VERIFIED))
                .thenReturn(Optional.empty());

        VerificationResponse response = verificationService.submitUserVerification(userId);

        assertThat(response.subjectType()).isEqualTo("USER");
        assertThat(response.subjectUserId()).isEqualTo(userId);
        assertThat(response.verificationType()).isEqualTo("MANUAL");
        assertThat(response.status()).isEqualTo("PENDING");

        ArgumentCaptor<VerificationEntity> captor = ArgumentCaptor.forClass(VerificationEntity.class);
        verify(verificationRepository).save(captor.capture());
        assertThat(captor.getValue().getSubjectUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getVerificationType()).isEqualTo("MANUAL");
    }

    @Test
    void submitWorkerVerificationSuccess() {
        UUID userId = UUID.randomUUID();
        UUID workerProfileId = UUID.randomUUID();
        WorkerProfileEntity workerProfile = new WorkerProfileEntity(workerProfileId, userId, "John Worker", "Delhi", AvailabilityStatus.AVAILABLE, false, NOW);

        when(workerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(workerProfile));
        when(verificationRepository.findFirstBySubjectWorkerProfileIdAndStatus(workerProfileId, VerificationStatus.PENDING))
                .thenReturn(Optional.empty());
        when(verificationRepository.findFirstBySubjectWorkerProfileIdAndStatus(workerProfileId, VerificationStatus.VERIFIED))
                .thenReturn(Optional.empty());

        VerificationResponse response = verificationService.submitWorkerVerification(userId);

        assertThat(response.subjectType()).isEqualTo("WORKER");
        assertThat(response.subjectWorkerProfileId()).isEqualTo(workerProfileId);
        assertThat(response.verificationType()).isEqualTo("MANUAL");
        assertThat(response.status()).isEqualTo("PENDING");
    }

    @Test
    void submitContractorVerificationSuccess() {
        UUID userId = UUID.randomUUID();
        UUID contractorProfileId = UUID.randomUUID();
        ContractorProfileEntity contractorProfile = new ContractorProfileEntity(contractorProfileId, userId, "ABC Construction", "Mumbai", NOW);

        when(contractorProfileRepository.findByUserId(userId)).thenReturn(Optional.of(contractorProfile));
        when(verificationRepository.findFirstBySubjectContractorProfileIdAndStatus(contractorProfileId, VerificationStatus.PENDING))
                .thenReturn(Optional.empty());

        VerificationResponse response = verificationService.submitContractorVerification(userId);

        assertThat(response.subjectType()).isEqualTo("CONTRACTOR");
        assertThat(response.subjectContractorProfileId()).isEqualTo(contractorProfileId);
        assertThat(response.verificationType()).isEqualTo("MANUAL");
    }

    @Test
    void submitClientVerificationSuccess() {
        UUID userId = UUID.randomUUID();
        UUID clientProfileId = UUID.randomUUID();
        ClientProfileEntity clientProfile = new ClientProfileEntity(clientProfileId, userId, ClientType.HOMEOWNER, "Jane Client", NOW);

        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(clientProfile));

        VerificationResponse response = verificationService.submitClientVerification(userId);

        assertThat(response.subjectType()).isEqualTo("CLIENT");
        assertThat(response.subjectClientProfileId()).isEqualTo(clientProfileId);
    }

    @Test
    void duplicatePendingVerificationFails() {
        UUID userId = UUID.randomUUID();
        VerificationEntity existingPending = new VerificationEntity(UUID.randomUUID(), SubjectType.USER, VerificationStatus.PENDING, NOW);

        when(verificationRepository.findFirstBySubjectUserIdAndStatus(userId, VerificationStatus.PENDING))
                .thenReturn(Optional.of(existingPending));

        assertThatThrownBy(() -> verificationService.submitUserVerification(userId))
                .isInstanceOf(VerificationException.class)
                .hasMessageContaining("already PENDING");
    }

    @Test
    void resubmissionAfterVerifiedFails() {
        UUID userId = UUID.randomUUID();
        VerificationEntity existingVerified = new VerificationEntity(UUID.randomUUID(), SubjectType.USER, VerificationStatus.VERIFIED, NOW);

        when(verificationRepository.findFirstBySubjectUserIdAndStatus(userId, VerificationStatus.PENDING))
                .thenReturn(Optional.empty());
        when(verificationRepository.findFirstBySubjectUserIdAndStatus(userId, VerificationStatus.VERIFIED))
                .thenReturn(Optional.of(existingVerified));

        assertThatThrownBy(() -> verificationService.submitUserVerification(userId))
                .isInstanceOf(VerificationException.class)
                .hasMessageContaining("already VERIFIED");
    }

    @Test
    void resubmissionAfterRejectedAllowed() {
        UUID userId = UUID.randomUUID();

        when(verificationRepository.findFirstBySubjectUserIdAndStatus(userId, VerificationStatus.PENDING))
                .thenReturn(Optional.empty());
        when(verificationRepository.findFirstBySubjectUserIdAndStatus(userId, VerificationStatus.VERIFIED))
                .thenReturn(Optional.empty());

        VerificationResponse response = verificationService.submitUserVerification(userId);
        assertThat(response.status()).isEqualTo("PENDING");
    }

    @Test
    void adminVerifySuccess() {
        UUID adminUserId = UUID.randomUUID();
        UUID verificationId = UUID.randomUUID();
        VerificationEntity entity = new VerificationEntity(verificationId, SubjectType.USER, VerificationStatus.PENDING, NOW);
        entity.setSubjectUserId(UUID.randomUUID());

        when(verificationRepository.findById(verificationId)).thenReturn(Optional.of(entity));

        VerificationResponse response = verificationService.verifyVerification(adminUserId, verificationId, "Approved KYC");

        assertThat(response.status()).isEqualTo("VERIFIED");
        assertThat(response.reviewedByUserId()).isEqualTo(adminUserId);
        assertThat(response.reviewedAt()).isEqualTo(NOW);
        assertThat(response.notes()).isEqualTo("Approved KYC");
    }

    @Test
    void adminRejectSuccess() {
        UUID adminUserId = UUID.randomUUID();
        UUID verificationId = UUID.randomUUID();
        VerificationEntity entity = new VerificationEntity(verificationId, SubjectType.WORKER, VerificationStatus.PENDING, NOW);

        when(verificationRepository.findById(verificationId)).thenReturn(Optional.of(entity));

        VerificationResponse response = verificationService.rejectVerification(adminUserId, verificationId, "Invalid documents");

        assertThat(response.status()).isEqualTo("REJECTED");
        assertThat(response.reviewedByUserId()).isEqualTo(adminUserId);
        assertThat(response.reviewedAt()).isEqualTo(NOW);
        assertThat(response.notes()).isEqualTo("Invalid documents");
    }

    @Test
    void reviewNonPendingVerificationFails() {
        UUID adminUserId = UUID.randomUUID();
        UUID verificationId = UUID.randomUUID();
        VerificationEntity entity = new VerificationEntity(verificationId, SubjectType.USER, VerificationStatus.VERIFIED, NOW);

        when(verificationRepository.findById(verificationId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> verificationService.verifyVerification(adminUserId, verificationId, "Notes"))
                .isInstanceOf(VerificationException.class)
                .hasMessageContaining("Only PENDING verification records can be reviewed");
    }

    @Test
    void getMyVerificationsReturnsSubjectRecords() {
        UUID userId = UUID.randomUUID();
        VerificationEntity userVer = new VerificationEntity(UUID.randomUUID(), SubjectType.USER, VerificationStatus.PENDING, NOW);
        userVer.setSubjectUserId(userId);

        when(verificationRepository.findBySubjectUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(userVer));

        List<VerificationResponse> myVerifications = verificationService.getMyVerifications(userId);

        assertThat(myVerifications).hasSize(1);
        assertThat(myVerifications.get(0).subjectUserId()).isEqualTo(userId);
    }
}
