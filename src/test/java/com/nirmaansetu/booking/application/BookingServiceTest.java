package com.nirmaansetu.booking.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nirmaansetu.booking.api.BookingResponse;
import com.nirmaansetu.booking.api.CreateBookingRequest;
import com.nirmaansetu.booking.domain.BookingException;
import com.nirmaansetu.booking.domain.BookingStatus;
import com.nirmaansetu.booking.domain.ProviderType;
import com.nirmaansetu.booking.infrastructure.persistence.BookingEntity;
import com.nirmaansetu.booking.infrastructure.persistence.BookingRepository;
import com.nirmaansetu.client.domain.ClientType;
import com.nirmaansetu.client.infrastructure.persistence.ClientProfileEntity;
import com.nirmaansetu.client.infrastructure.persistence.ClientProfileRepository;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorProfileEntity;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorProfileRepository;
import com.nirmaansetu.project.domain.ProjectStatus;
import com.nirmaansetu.project.infrastructure.persistence.ProjectEntity;
import com.nirmaansetu.project.infrastructure.persistence.ProjectRepository;
import com.nirmaansetu.requirement.domain.RequirementStatus;
import com.nirmaansetu.requirement.domain.WorkerType;
import com.nirmaansetu.requirement.infrastructure.persistence.WorkforceRequirementEntity;
import com.nirmaansetu.requirement.infrastructure.persistence.WorkforceRequirementRepository;
import com.nirmaansetu.team.domain.TeamStatus;
import com.nirmaansetu.team.infrastructure.persistence.TeamEntity;
import com.nirmaansetu.team.infrastructure.persistence.TeamRepository;
import com.nirmaansetu.worker.domain.AvailabilityStatus;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileEntity;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-26T12:00:00Z");

    @Mock private BookingRepository bookingRepository;
    @Mock private WorkforceRequirementRepository requirementRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private ClientProfileRepository clientProfileRepository;
    @Mock private WorkerProfileRepository workerProfileRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private ContractorProfileRepository contractorProfileRepository;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
            bookingRepository, requirementRepository, projectRepository, clientProfileRepository,
            workerProfileRepository, teamRepository, contractorProfileRepository, Clock.fixed(NOW, ZoneOffset.UTC)
        );
        when(bookingRepository.save(any(BookingEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(requirementRepository.save(any(WorkforceRequirementEntity.class))).thenAnswer(i -> i.getArgument(0));
    }

    private ClientProfileEntity clientProfileFor(UUID userId) {
        return new ClientProfileEntity(UUID.randomUUID(), userId, ClientType.HOMEOWNER, "Jane Client", NOW);
    }

    private ProjectEntity projectFor(UUID clientProfileId) {
        return new ProjectEntity(UUID.randomUUID(), clientProfileId, "Villa Project", ProjectStatus.ACTIVE, "Mohali", NOW);
    }

    private WorkforceRequirementEntity openRequirementFor(UUID projectId, int quantity) {
        return new WorkforceRequirementEntity(
            UUID.randomUUID(), projectId, "Mohali", LocalDate.of(2026, 9, 1), 30,
            WorkerType.SKILLED_WORKER, UUID.randomUUID(), quantity, "INR", false, false, RequirementStatus.OPEN, NOW
        );
    }

    private WorkforceRequirementEntity draftRequirementFor(UUID projectId) {
        return new WorkforceRequirementEntity(
            UUID.randomUUID(), projectId, "Mohali", LocalDate.of(2026, 9, 1), 30,
            WorkerType.SKILLED_WORKER, UUID.randomUUID(), 5, "INR", false, false, RequirementStatus.DRAFT, NOW
        );
    }

    @Test
    void createWorkerBookingSuccess() {
        UUID userId = UUID.randomUUID();
        UUID workerUserId = UUID.randomUUID();
        UUID workerProfileId = UUID.randomUUID();

        ClientProfileEntity client = clientProfileFor(userId);
        ProjectEntity project = projectFor(client.getId());
        WorkforceRequirementEntity requirement = openRequirementFor(project.getId(), 5);

        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(client));
        when(requirementRepository.findById(requirement.getId())).thenReturn(Optional.of(requirement));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(workerProfileRepository.existsById(workerProfileId)).thenReturn(true);

        CreateBookingRequest request = new CreateBookingRequest("WORKER", workerProfileId, null, null, 1);

        BookingResponse response = bookingService.createBooking(userId, requirement.getId(), request);

        assertThat(response.requirementId()).isEqualTo(requirement.getId());
        assertThat(response.providerType()).isEqualTo("WORKER");
        assertThat(response.providerWorkerProfileId()).isEqualTo(workerProfileId);
        assertThat(response.quantity()).isEqualTo(1);
        assertThat(response.status()).isEqualTo("PENDING");
    }

    @Test
    void createWorkerBookingWithQuantityGreaterThanOneFails() {
        UUID userId = UUID.randomUUID();
        UUID workerProfileId = UUID.randomUUID();

        ClientProfileEntity client = clientProfileFor(userId);
        ProjectEntity project = projectFor(client.getId());
        WorkforceRequirementEntity requirement = openRequirementFor(project.getId(), 5);

        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(client));
        when(requirementRepository.findById(requirement.getId())).thenReturn(Optional.of(requirement));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        CreateBookingRequest request = new CreateBookingRequest("WORKER", workerProfileId, null, null, 3);

        assertThatThrownBy(() -> bookingService.createBooking(userId, requirement.getId(), request))
            .isInstanceOf(BookingException.class)
            .hasMessageContaining("Worker booking quantity must equal 1");
    }

    @Test
    void createBookingAgainstDraftRequirementFails() {
        UUID userId = UUID.randomUUID();
        UUID workerProfileId = UUID.randomUUID();

        ClientProfileEntity client = clientProfileFor(userId);
        ProjectEntity project = projectFor(client.getId());
        WorkforceRequirementEntity requirement = draftRequirementFor(project.getId());

        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(client));
        when(requirementRepository.findById(requirement.getId())).thenReturn(Optional.of(requirement));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        CreateBookingRequest request = new CreateBookingRequest("WORKER", workerProfileId, null, null, 1);

        assertThatThrownBy(() -> bookingService.createBooking(userId, requirement.getId(), request))
            .isInstanceOf(BookingException.class)
            .hasMessageContaining("Bookings can only be created for OPEN requirements");
    }

    @Test
    void workerOwnerCanAcceptWorkerBooking() {
        UUID workerUserId = UUID.randomUUID();
        UUID workerProfileId = UUID.randomUUID();
        UUID reqId = UUID.randomUUID();

        WorkerProfileEntity workerProfile = new WorkerProfileEntity(workerProfileId, workerUserId, "John Worker", "Mohali", AvailabilityStatus.AVAILABLE, false, NOW);
        WorkforceRequirementEntity requirement = openRequirementFor(UUID.randomUUID(), 5);

        BookingEntity booking = new BookingEntity(UUID.randomUUID(), reqId, ProviderType.WORKER, 1, BookingStatus.PENDING, NOW);
        booking.setProviderWorkerProfileId(workerProfileId);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(workerProfileRepository.findById(workerProfileId)).thenReturn(Optional.of(workerProfile));
        when(requirementRepository.findByIdWithLock(reqId)).thenReturn(Optional.of(requirement));
        when(bookingRepository.sumAcceptedQuantityByRequirementId(reqId)).thenReturn(0);

        BookingResponse response = bookingService.acceptBooking(workerUserId, booking.getId());

        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(response.respondedAt()).isNotNull();
    }

    @Test
    void nonOwnerWorkerCannotAcceptWorkerBooking() {
        UUID workerUserId = UUID.randomUUID();
        UUID attackerUserId = UUID.randomUUID();
        UUID workerProfileId = UUID.randomUUID();
        UUID reqId = UUID.randomUUID();

        WorkerProfileEntity workerProfile = new WorkerProfileEntity(workerProfileId, workerUserId, "John Worker", "Mohali", AvailabilityStatus.AVAILABLE, false, NOW);

        BookingEntity booking = new BookingEntity(UUID.randomUUID(), reqId, ProviderType.WORKER, 1, BookingStatus.PENDING, NOW);
        booking.setProviderWorkerProfileId(workerProfileId);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(workerProfileRepository.findById(workerProfileId)).thenReturn(Optional.of(workerProfile));

        assertThatThrownBy(() -> bookingService.acceptBooking(attackerUserId, booking.getId()))
            .isInstanceOf(BookingException.class)
            .hasMessageContaining("Access denied");
    }

    @Test
    void workerTeamManagerCanAcceptTeamBooking() {
        UUID managerUserId = UUID.randomUUID(); // WORKER user who is team manager
        UUID teamId = UUID.randomUUID();
        UUID reqId = UUID.randomUUID();

        TeamEntity team = new TeamEntity(teamId, managerUserId, "Alpha Team", TeamStatus.ACTIVE, NOW);
        WorkforceRequirementEntity requirement = openRequirementFor(UUID.randomUUID(), 5);

        BookingEntity booking = new BookingEntity(UUID.randomUUID(), reqId, ProviderType.TEAM, 3, BookingStatus.PENDING, NOW);
        booking.setProviderTeamId(teamId);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(requirementRepository.findByIdWithLock(reqId)).thenReturn(Optional.of(requirement));
        when(bookingRepository.sumAcceptedQuantityByRequirementId(reqId)).thenReturn(0);

        BookingResponse response = bookingService.acceptBooking(managerUserId, booking.getId());

        assertThat(response.status()).isEqualTo("ACCEPTED");
    }

    @Test
    void contractorTeamManagerCanAcceptTeamBooking() {
        UUID managerUserId = UUID.randomUUID(); // CONTRACTOR user who is team manager
        UUID teamId = UUID.randomUUID();
        UUID reqId = UUID.randomUUID();

        TeamEntity team = new TeamEntity(teamId, managerUserId, "Beta Team", TeamStatus.ACTIVE, NOW);
        WorkforceRequirementEntity requirement = openRequirementFor(UUID.randomUUID(), 5);

        BookingEntity booking = new BookingEntity(UUID.randomUUID(), reqId, ProviderType.TEAM, 3, BookingStatus.PENDING, NOW);
        booking.setProviderTeamId(teamId);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(requirementRepository.findByIdWithLock(reqId)).thenReturn(Optional.of(requirement));
        when(bookingRepository.sumAcceptedQuantityByRequirementId(reqId)).thenReturn(0);

        BookingResponse response = bookingService.acceptBooking(managerUserId, booking.getId());

        assertThat(response.status()).isEqualTo("ACCEPTED");
    }

    @Test
    void nonManagerWorkerCannotAcceptTeamBooking() {
        UUID managerUserId = UUID.randomUUID();
        UUID nonManagerUserId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID reqId = UUID.randomUUID();

        TeamEntity team = new TeamEntity(teamId, managerUserId, "Alpha Team", TeamStatus.ACTIVE, NOW);

        BookingEntity booking = new BookingEntity(UUID.randomUUID(), reqId, ProviderType.TEAM, 3, BookingStatus.PENDING, NOW);
        booking.setProviderTeamId(teamId);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> bookingService.acceptBooking(nonManagerUserId, booking.getId()))
            .isInstanceOf(BookingException.class)
            .hasMessageContaining("You are not the manager of this team");
    }

    @Test
    void pendingBookingsDoNotReserveCapacityAndRejectedBookingsDoNotCount() {
        UUID managerUserId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID reqId = UUID.randomUUID();

        TeamEntity team = new TeamEntity(teamId, managerUserId, "Alpha Team", TeamStatus.ACTIVE, NOW);
        WorkforceRequirementEntity requirement = openRequirementFor(UUID.randomUUID(), 5);

        BookingEntity bookingA = new BookingEntity(UUID.randomUUID(), reqId, ProviderType.TEAM, 3, BookingStatus.PENDING, NOW);
        bookingA.setProviderTeamId(teamId);

        when(bookingRepository.findById(bookingA.getId())).thenReturn(Optional.of(bookingA));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(requirementRepository.findByIdWithLock(reqId)).thenReturn(Optional.of(requirement));
        // Accepted sum is 0 despite pending or rejected bookings existing
        when(bookingRepository.sumAcceptedQuantityByRequirementId(reqId)).thenReturn(0);

        BookingResponse response = bookingService.acceptBooking(managerUserId, bookingA.getId());

        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(requirement.getStatus()).isEqualTo(RequirementStatus.OPEN);
    }

    @Test
    void acceptBookingOverbookingFails() {
        UUID managerUserId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID reqId = UUID.randomUUID();  // used as both booking.requirementId and requirement.id

        TeamEntity team = new TeamEntity(teamId, managerUserId, "Alpha Team", TeamStatus.ACTIVE, NOW);
        // Requirement entity has the same reqId so that sumAccepted mock matches requirement.getId()
        WorkforceRequirementEntity requirement = new WorkforceRequirementEntity(
            reqId, UUID.randomUUID(), "Mohali", LocalDate.of(2026, 9, 1), 30,
            WorkerType.SKILLED_WORKER, UUID.randomUUID(), 5, "INR", false, false, RequirementStatus.OPEN, NOW
        );

        BookingEntity bookingB = new BookingEntity(UUID.randomUUID(), reqId, ProviderType.TEAM, 4, BookingStatus.PENDING, NOW);
        bookingB.setProviderTeamId(teamId);

        when(bookingRepository.findById(bookingB.getId())).thenReturn(Optional.of(bookingB));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(requirementRepository.findByIdWithLock(reqId)).thenReturn(Optional.of(requirement));
        // Already accepted 3 out of 5
        when(bookingRepository.sumAcceptedQuantityByRequirementId(reqId)).thenReturn(3);

        // 3 + 4 > 5 -> should fail
        assertThatThrownBy(() -> bookingService.acceptBooking(managerUserId, bookingB.getId()))
            .isInstanceOf(BookingException.class)
            .hasMessageContaining("exceeds the requirement quantity");
    }

    @Test
    void openRequirementAutomaticallyFulfillsWhenAcceptedQuantityReachesRequirementQuantity() {
        UUID managerUserId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID reqId = UUID.randomUUID();  // shared ID for booking.requirementId and requirement.id

        TeamEntity team = new TeamEntity(teamId, managerUserId, "Alpha Team", TeamStatus.ACTIVE, NOW);
        // Requirement entity has the same reqId so that sumAccepted mock matches requirement.getId()
        WorkforceRequirementEntity requirement = new WorkforceRequirementEntity(
            reqId, UUID.randomUUID(), "Mohali", LocalDate.of(2026, 9, 1), 30,
            WorkerType.SKILLED_WORKER, UUID.randomUUID(), 5, "INR", false, false, RequirementStatus.OPEN, NOW
        );

        BookingEntity booking = new BookingEntity(UUID.randomUUID(), reqId, ProviderType.TEAM, 2, BookingStatus.PENDING, NOW);
        booking.setProviderTeamId(teamId);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(requirementRepository.findByIdWithLock(reqId)).thenReturn(Optional.of(requirement));
        // Currently accepted is 3; adding 2 makes 5 == requirement quantity 5
        when(bookingRepository.sumAcceptedQuantityByRequirementId(reqId)).thenReturn(3);

        BookingResponse response = bookingService.acceptBooking(managerUserId, booking.getId());

        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(requirement.getStatus()).isEqualTo(RequirementStatus.FULFILLED);
    }
}
