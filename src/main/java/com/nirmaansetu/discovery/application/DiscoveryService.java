package com.nirmaansetu.discovery.application;

import com.nirmaansetu.contractor.infrastructure.persistence.ContractorProfileEntity;
import com.nirmaansetu.contractor.infrastructure.persistence.ContractorProfileRepository;
import com.nirmaansetu.discovery.api.ContractorDiscoveryResponse;
import com.nirmaansetu.discovery.api.RequirementDiscoveryResponse;
import com.nirmaansetu.discovery.api.TeamDiscoveryResponse;
import com.nirmaansetu.discovery.api.WorkerDiscoveryResponse;
import com.nirmaansetu.discovery.domain.DiscoveryException;
import com.nirmaansetu.requirement.domain.RequirementStatus;
import com.nirmaansetu.requirement.domain.WorkerType;
import com.nirmaansetu.requirement.infrastructure.persistence.WorkforceRequirementEntity;
import com.nirmaansetu.requirement.infrastructure.persistence.WorkforceRequirementRepository;
import com.nirmaansetu.team.domain.TeamStatus;
import com.nirmaansetu.team.infrastructure.persistence.TeamEntity;
import com.nirmaansetu.team.infrastructure.persistence.TeamMemberRepository;
import com.nirmaansetu.team.infrastructure.persistence.TeamRepository;
import com.nirmaansetu.verification.domain.VerificationStatus;
import com.nirmaansetu.verification.infrastructure.persistence.VerificationRepository;
import com.nirmaansetu.worker.domain.AvailabilityStatus;
import com.nirmaansetu.worker.infrastructure.persistence.SkillEntity;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileEntity;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class DiscoveryService {

    private static final int MAX_PAGE_SIZE = 50;

    private final WorkerProfileRepository workerProfileRepository;
    private final ContractorProfileRepository contractorProfileRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final WorkforceRequirementRepository requirementRepository;
    private final VerificationRepository verificationRepository;

    public DiscoveryService(WorkerProfileRepository workerProfileRepository,
                            ContractorProfileRepository contractorProfileRepository,
                            TeamRepository teamRepository,
                            TeamMemberRepository teamMemberRepository,
                            WorkforceRequirementRepository requirementRepository,
                            VerificationRepository verificationRepository) {
        this.workerProfileRepository = workerProfileRepository;
        this.contractorProfileRepository = contractorProfileRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.requirementRepository = requirementRepository;
        this.verificationRepository = verificationRepository;
    }

    private Pageable sanitizePageable(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            return PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort());
        }
        return pageable;
    }

    @Transactional(readOnly = true)
    public Page<WorkerDiscoveryResponse> searchWorkers(String location, UUID skillId,
                                                      AvailabilityStatus availabilityStatus,
                                                      Integer minExperience, BigDecimal maxDailyRate,
                                                      Boolean isTravelWilling, Boolean isVerified,
                                                      Pageable pageable) {
        Pageable validPageable = sanitizePageable(pageable);
        AvailabilityStatus targetStatus = availabilityStatus != null ? availabilityStatus : AvailabilityStatus.AVAILABLE;

        Stream<WorkerProfileEntity> stream = workerProfileRepository.findAll().stream();

        stream = stream.filter(w -> w.getAvailabilityStatus() == targetStatus);

        if (location != null && !location.isBlank()) {
            String locLower = location.trim().toLowerCase();
            stream = stream.filter(w -> w.getLocation() != null && w.getLocation().toLowerCase().contains(locLower));
        }

        if (skillId != null) {
            stream = stream.filter(w -> w.getSkills() != null &&
                    w.getSkills().stream().map(SkillEntity::getId).anyMatch(id -> id.equals(skillId)));
        }

        if (minExperience != null) {
            stream = stream.filter(w -> w.getExperienceYears() != null && w.getExperienceYears() >= minExperience);
        }

        if (maxDailyRate != null) {
            stream = stream.filter(w -> w.getDailyRate() != null && w.getDailyRate().compareTo(maxDailyRate) <= 0);
        }

        if (isTravelWilling != null) {
            stream = stream.filter(w -> w.isTravelWilling() == isTravelWilling);
        }

        if (isVerified != null) {
            stream = stream.filter(w -> {
                boolean verified = verificationRepository.findFirstBySubjectWorkerProfileIdAndStatus(w.getId(), VerificationStatus.VERIFIED).isPresent();
                return verified == isVerified;
            });
        }

        List<WorkerProfileEntity> filtered = stream
                .sorted(Comparator.comparing(WorkerProfileEntity::getExperienceYears, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(WorkerProfileEntity::getCreatedAt, Comparator.reverseOrder()))
                .toList();

        return paginateList(filtered, validPageable).map(w -> {
            boolean verified = verificationRepository.findFirstBySubjectWorkerProfileIdAndStatus(w.getId(), VerificationStatus.VERIFIED).isPresent();
            List<UUID> skillIds = w.getSkills() != null ? w.getSkills().stream().map(SkillEntity::getId).toList() : List.of();
            return new WorkerDiscoveryResponse(
                    w.getId(),
                    w.getDisplayName(),
                    w.getExperienceYears(),
                    w.getLocation(),
                    w.getAvailabilityStatus().name(),
                    w.getDailyRate(),
                    w.getProfileDescription(),
                    w.isTravelWilling(),
                    skillIds,
                    verified
            );
        });
    }

    @Transactional(readOnly = true)
    public Page<ContractorDiscoveryResponse> searchContractors(String location, String name, Boolean isVerified, Pageable pageable) {
        Pageable validPageable = sanitizePageable(pageable);
        Stream<ContractorProfileEntity> stream = contractorProfileRepository.findAll().stream();

        if (location != null && !location.isBlank()) {
            String locLower = location.trim().toLowerCase();
            stream = stream.filter(c -> c.getLocation() != null && c.getLocation().toLowerCase().contains(locLower));
        }

        if (name != null && !name.isBlank()) {
            String nameLower = name.trim().toLowerCase();
            stream = stream.filter(c -> c.getDisplayName() != null && c.getDisplayName().toLowerCase().contains(nameLower));
        }

        if (isVerified != null) {
            stream = stream.filter(c -> {
                boolean verified = verificationRepository.findFirstBySubjectContractorProfileIdAndStatus(c.getId(), VerificationStatus.VERIFIED).isPresent();
                return verified == isVerified;
            });
        }

        List<ContractorProfileEntity> filtered = stream
                .sorted(Comparator.comparing(ContractorProfileEntity::getDisplayName, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ContractorProfileEntity::getCreatedAt, Comparator.reverseOrder()))
                .toList();

        return paginateList(filtered, validPageable).map(c -> {
            boolean verified = verificationRepository.findFirstBySubjectContractorProfileIdAndStatus(c.getId(), VerificationStatus.VERIFIED).isPresent();
            return new ContractorDiscoveryResponse(
                    c.getId(),
                    c.getDisplayName(),
                    c.getDescription(),
                    c.getLocation(),
                    verified
            );
        });
    }

    @Transactional(readOnly = true)
    public Page<TeamDiscoveryResponse> searchTeams(String location, String name, Pageable pageable) {
        Pageable validPageable = sanitizePageable(pageable);
        Stream<TeamEntity> stream = teamRepository.findAll().stream();

        stream = stream.filter(t -> t.getStatus() == TeamStatus.ACTIVE);

        if (name != null && !name.isBlank()) {
            String nameLower = name.trim().toLowerCase();
            stream = stream.filter(t -> t.getName() != null && t.getName().toLowerCase().contains(nameLower));
        }

        List<TeamEntity> filtered = stream
                .sorted(Comparator.comparing(TeamEntity::getName, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(TeamEntity::getCreatedAt, Comparator.reverseOrder()))
                .toList();

        return paginateList(filtered, validPageable).map(t -> {
            long activeMemberCount = teamMemberRepository.findByIdTeamId(t.getId())
                    .stream()
                    .filter(m -> m.getEndsOn() == null)
                    .count();
            return new TeamDiscoveryResponse(
                    t.getId(),
                    t.getManagerUserId(),
                    t.getName(),
                    t.getDescription(),
                    t.getStatus().name(),
                    activeMemberCount
            );
        });
    }

    @Transactional(readOnly = true)
    public Page<RequirementDiscoveryResponse> searchRequirements(String location, WorkerType workerType, UUID skillId,
                                                                 BigDecimal minDailyRate, BigDecimal maxDailyRate,
                                                                 Boolean accommodationAvailable, Boolean foodAvailable,
                                                                 Pageable pageable) {
        Pageable validPageable = sanitizePageable(pageable);

        // Explicit enforcement: strictly OPEN requirements only
        Stream<WorkforceRequirementEntity> stream = requirementRepository.findAll().stream()
                .filter(r -> r.getStatus() == RequirementStatus.OPEN);

        if (location != null && !location.isBlank()) {
            String locLower = location.trim().toLowerCase();
            stream = stream.filter(r -> r.getLocation() != null && r.getLocation().toLowerCase().contains(locLower));
        }

        if (workerType != null) {
            stream = stream.filter(r -> r.getWorkerType() == workerType);
        }

        if (skillId != null) {
            stream = stream.filter(r -> r.getSkillId() != null && r.getSkillId().equals(skillId));
        }

        if (minDailyRate != null) {
            stream = stream.filter(r -> r.getDailyRate() != null && r.getDailyRate().compareTo(minDailyRate) >= 0);
        }

        if (maxDailyRate != null) {
            stream = stream.filter(r -> r.getDailyRate() != null && r.getDailyRate().compareTo(maxDailyRate) <= 0);
        }

        if (accommodationAvailable != null) {
            stream = stream.filter(r -> r.getAccommodationAvailable() == accommodationAvailable);
        }

        if (foodAvailable != null) {
            stream = stream.filter(r -> r.getFoodAvailable() == foodAvailable);
        }

        List<WorkforceRequirementEntity> filtered = stream
                .sorted(Comparator.comparing(WorkforceRequirementEntity::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(WorkforceRequirementEntity::getCreatedAt, Comparator.reverseOrder()))
                .toList();

        return paginateList(filtered, validPageable).map(r -> new RequirementDiscoveryResponse(
                r.getId(),
                r.getProjectId(),
                r.getLocation(),
                r.getStartDate(),
                r.getDurationDays(),
                r.getWorkerType().name(),
                r.getSkillId(),
                r.getQuantity(),
                r.getDailyRate(),
                r.getBudgetAmount(),
                r.getCurrencyCode(),
                r.getAccommodationAvailable(),
                r.getFoodAvailable(),
                r.getAdditionalNotes(),
                r.getStatus().name(),
                r.getCreatedAt()
        ));
    }

    private <T> Page<T> paginateList(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        if (start >= list.size()) {
            return new PageImpl<>(List.of(), pageable, list.size());
        }
        int end = Math.min((start + pageable.getPageSize()), list.size());
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }
}
