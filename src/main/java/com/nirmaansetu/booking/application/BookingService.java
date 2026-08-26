package com.nirmaansetu.booking.application;

import com.nirmaansetu.booking.api.BookingResponse;
import com.nirmaansetu.booking.api.CreateBookingRequest;
import com.nirmaansetu.booking.domain.BookingException;
import com.nirmaansetu.booking.domain.BookingStatus;
import com.nirmaansetu.booking.domain.ProviderType;
import com.nirmaansetu.booking.infrastructure.persistence.BookingEntity;
import com.nirmaansetu.booking.infrastructure.persistence.BookingRepository;
import com.nirmaansetu.client.infrastructure.persistence.ClientProfileEntity;
import com.nirmaansetu.client.infrastructure.persistence.ClientProfileRepository;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorProfileEntity;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorProfileRepository;
import com.nirmaansetu.project.infrastructure.persistence.ProjectEntity;
import com.nirmaansetu.project.infrastructure.persistence.ProjectRepository;
import com.nirmaansetu.requirement.domain.RequirementStatus;
import com.nirmaansetu.requirement.infrastructure.persistence.WorkforceRequirementEntity;
import com.nirmaansetu.requirement.infrastructure.persistence.WorkforceRequirementRepository;
import com.nirmaansetu.team.infrastructure.persistence.TeamEntity;
import com.nirmaansetu.team.infrastructure.persistence.TeamRepository;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileEntity;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final WorkforceRequirementRepository requirementRepository;
    private final ProjectRepository projectRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final TeamRepository teamRepository;
    private final ContractorProfileRepository contractorProfileRepository;
    private final Clock clock;

    public BookingService(BookingRepository bookingRepository,
                          WorkforceRequirementRepository requirementRepository,
                          ProjectRepository projectRepository,
                          ClientProfileRepository clientProfileRepository,
                          WorkerProfileRepository workerProfileRepository,
                          TeamRepository teamRepository,
                          ContractorProfileRepository contractorProfileRepository,
                          Clock clock) {
        this.bookingRepository = bookingRepository;
        this.requirementRepository = requirementRepository;
        this.projectRepository = projectRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.workerProfileRepository = workerProfileRepository;
        this.teamRepository = teamRepository;
        this.contractorProfileRepository = contractorProfileRepository;
        this.clock = clock;
    }

    @Transactional
    public BookingResponse createBooking(UUID userId, UUID requirementId, CreateBookingRequest request) {
        WorkforceRequirementEntity requirement = findRequirementAndVerifyClientOwner(userId, requirementId);

        if (requirement.getStatus() != RequirementStatus.OPEN) {
            throw new BookingException("Bookings can only be created for OPEN requirements.");
        }

        ProviderType providerType = parseProviderType(request.providerType());
        validateProviderAndQuantity(providerType, request);

        Instant now = clock.instant();
        BookingEntity entity = new BookingEntity(
            UUID.randomUUID(),
            requirement.getId(),
            providerType,
            request.quantity(),
            BookingStatus.PENDING,
            now
        );

        if (providerType == ProviderType.WORKER) {
            entity.setProviderWorkerProfileId(request.providerWorkerProfileId());
        } else if (providerType == ProviderType.TEAM) {
            entity.setProviderTeamId(request.providerTeamId());
        } else if (providerType == ProviderType.CONTRACTOR) {
            entity.setProviderContractorProfileId(request.providerContractorProfileId());
        }

        return toResponse(bookingRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getRequirementBookings(UUID userId, UUID requirementId) {
        WorkforceRequirementEntity requirement = findRequirementAndVerifyClientOwner(userId, requirementId);
        return bookingRepository.findByRequirementId(requirement.getId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID userId, UUID bookingId) {
        BookingEntity booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingException("Booking not found."));

        boolean isClientOwner = isClientOwnerOfRequirement(userId, booking.getRequirementId());
        boolean isProviderOwner = isProviderOwnerOfBooking(userId, booking);

        if (!isClientOwner && !isProviderOwner) {
            throw new BookingException("Access denied. You are not authorized to view this booking.");
        }

        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getProviderBookings(UUID userId) {
        List<BookingEntity> results = new ArrayList<>();

        Optional<WorkerProfileEntity> workerOpt = workerProfileRepository.findByUserId(userId);
        workerOpt.ifPresent(worker ->
            results.addAll(bookingRepository.findByProviderWorkerProfileId(worker.getId()))
        );

        List<TeamEntity> teams = teamRepository.findByManagerUserId(userId);
        if (!teams.isEmpty()) {
            List<UUID> teamIds = teams.stream().map(TeamEntity::getId).toList();
            results.addAll(bookingRepository.findByProviderTeamIdIn(teamIds));
        }

        Optional<ContractorProfileEntity> contractorOpt = contractorProfileRepository.findByUserId(userId);
        contractorOpt.ifPresent(contractor ->
            results.addAll(bookingRepository.findByProviderContractorProfileId(contractor.getId()))
        );

        return results.stream()
            .distinct()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public BookingResponse acceptBooking(UUID userId, UUID bookingId) {
        BookingEntity booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingException("Booking not found."));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingException("Booking is not in PENDING status.");
        }

        verifyProviderOwnerOfBooking(userId, booking);

        // Pessimistic Write Lock on parent requirement
        WorkforceRequirementEntity requirement = requirementRepository.findByIdWithLock(booking.getRequirementId())
            .orElseThrow(() -> new BookingException("Associated requirement not found."));

        if (requirement.getStatus() != RequirementStatus.OPEN) {
            throw new BookingException("Cannot accept booking for non-OPEN requirement.");
        }

        int currentlyAccepted = bookingRepository.sumAcceptedQuantityByRequirementId(requirement.getId());
        if (currentlyAccepted + booking.getQuantity() > requirement.getQuantity()) {
            throw new BookingException("Accepting this booking exceeds the requirement quantity.");
        }

        Instant now = clock.instant();
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setRespondedAt(now);
        booking.setUpdatedAt(now);
        BookingEntity savedBooking = bookingRepository.save(booking);

        if (currentlyAccepted + booking.getQuantity() == requirement.getQuantity()) {
            requirement.setStatus(RequirementStatus.FULFILLED);
            requirement.setUpdatedAt(now);
            requirementRepository.save(requirement);
        }

        return toResponse(savedBooking);
    }

    @Transactional
    public BookingResponse rejectBooking(UUID userId, UUID bookingId) {
        BookingEntity booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingException("Booking not found."));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingException("Booking is not in PENDING status.");
        }

        verifyProviderOwnerOfBooking(userId, booking);

        Instant now = clock.instant();
        booking.setStatus(BookingStatus.REJECTED);
        booking.setRespondedAt(now);
        booking.setUpdatedAt(now);

        return toResponse(bookingRepository.save(booking));
    }

    private WorkforceRequirementEntity findRequirementAndVerifyClientOwner(UUID userId, UUID requirementId) {
        ClientProfileEntity clientProfile = clientProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new BookingException("Client profile not found."));

        WorkforceRequirementEntity requirement = requirementRepository.findById(requirementId)
            .orElseThrow(() -> new BookingException("Workforce requirement not found."));

        ProjectEntity project = projectRepository.findById(requirement.getProjectId())
            .orElseThrow(() -> new BookingException("Project not found."));

        if (!project.getClientProfileId().equals(clientProfile.getId())) {
            throw new BookingException("Access denied. You do not own this project/requirement.");
        }

        return requirement;
    }

    private boolean isClientOwnerOfRequirement(UUID userId, UUID requirementId) {
        try {
            findRequirementAndVerifyClientOwner(userId, requirementId);
            return true;
        } catch (BookingException ex) {
            return false;
        }
    }

    private boolean isProviderOwnerOfBooking(UUID userId, BookingEntity booking) {
        try {
            verifyProviderOwnerOfBooking(userId, booking);
            return true;
        } catch (BookingException ex) {
            return false;
        }
    }

    private void verifyProviderOwnerOfBooking(UUID userId, BookingEntity booking) {
        if (booking.getProviderType() == ProviderType.WORKER) {
            UUID workerProfileId = booking.getProviderWorkerProfileId();
            WorkerProfileEntity worker = workerProfileRepository.findById(workerProfileId)
                .orElseThrow(() -> new BookingException("Worker profile not found."));
            if (!worker.getUserId().equals(userId)) {
                throw new BookingException("Access denied. You do not own this worker profile.");
            }
        } else if (booking.getProviderType() == ProviderType.TEAM) {
            UUID teamId = booking.getProviderTeamId();
            TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BookingException("Team not found."));
            if (!team.getManagerUserId().equals(userId)) {
                throw new BookingException("Access denied. You are not the manager of this team.");
            }
        } else if (booking.getProviderType() == ProviderType.CONTRACTOR) {
            UUID contractorProfileId = booking.getProviderContractorProfileId();
            ContractorProfileEntity contractor = contractorProfileRepository.findById(contractorProfileId)
                .orElseThrow(() -> new BookingException("Contractor profile not found."));
            if (!contractor.getUserId().equals(userId)) {
                throw new BookingException("Access denied. You do not own this contractor profile.");
            }
        }
    }

    private ProviderType parseProviderType(String raw) {
        try {
            return ProviderType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new BookingException("Invalid provider type. Allowed values: WORKER, TEAM, CONTRACTOR.");
        }
    }

    private void validateProviderAndQuantity(ProviderType providerType, CreateBookingRequest request) {
        if (providerType == ProviderType.WORKER) {
            if (request.providerWorkerProfileId() == null || request.providerTeamId() != null || request.providerContractorProfileId() != null) {
                throw new BookingException("Exactly one provider reference (providerWorkerProfileId) must be provided for WORKER provider type.");
            }
            if (request.quantity() != 1) {
                throw new BookingException("Worker booking quantity must equal 1.");
            }
            if (!workerProfileRepository.existsById(request.providerWorkerProfileId())) {
                throw new BookingException("Referenced worker profile does not exist.");
            }
        } else if (providerType == ProviderType.TEAM) {
            if (request.providerTeamId() == null || request.providerWorkerProfileId() != null || request.providerContractorProfileId() != null) {
                throw new BookingException("Exactly one provider reference (providerTeamId) must be provided for TEAM provider type.");
            }
            if (request.quantity() < 1) {
                throw new BookingException("Quantity must be at least 1.");
            }
            if (!teamRepository.existsById(request.providerTeamId())) {
                throw new BookingException("Referenced team does not exist.");
            }
        } else if (providerType == ProviderType.CONTRACTOR) {
            if (request.providerContractorProfileId() == null || request.providerWorkerProfileId() != null || request.providerTeamId() != null) {
                throw new BookingException("Exactly one provider reference (providerContractorProfileId) must be provided for CONTRACTOR provider type.");
            }
            if (request.quantity() < 1) {
                throw new BookingException("Quantity must be at least 1.");
            }
            if (!contractorProfileRepository.existsById(request.providerContractorProfileId())) {
                throw new BookingException("Referenced contractor profile does not exist.");
            }
        }
    }

    private BookingResponse toResponse(BookingEntity entity) {
        return new BookingResponse(
            entity.getId(),
            entity.getRequirementId(),
            entity.getProviderType().name(),
            entity.getProviderWorkerProfileId(),
            entity.getProviderTeamId(),
            entity.getProviderContractorProfileId(),
            entity.getQuantity(),
            entity.getStatus().name(),
            entity.getRequestedAt(),
            entity.getRespondedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
