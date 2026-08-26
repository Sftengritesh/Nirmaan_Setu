package com.nirmaansetu.discovery.api;

import com.nirmaansetu.discovery.application.DiscoveryService;
import com.nirmaansetu.requirement.domain.WorkerType;
import com.nirmaansetu.worker.domain.AvailabilityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    public DiscoveryController(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @GetMapping("/api/discovery/workers")
    @PreAuthorize("hasAnyRole('CLIENT', 'CONTRACTOR', 'ADMIN')")
    public Page<WorkerDiscoveryResponse> searchWorkers(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) UUID skillId,
            @RequestParam(required = false) AvailabilityStatus availabilityStatus,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) BigDecimal maxDailyRate,
            @RequestParam(required = false) Boolean isTravelWilling,
            @RequestParam(required = false) Boolean isVerified,
            Pageable pageable) {
        return discoveryService.searchWorkers(location, skillId, availabilityStatus, minExperience, maxDailyRate, isTravelWilling, isVerified, pageable);
    }

    @GetMapping("/api/discovery/contractors")
    @PreAuthorize("hasAnyRole('CLIENT', 'WORKER', 'ADMIN')")
    public Page<ContractorDiscoveryResponse> searchContractors(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isVerified,
            Pageable pageable) {
        return discoveryService.searchContractors(location, name, isVerified, pageable);
    }

    @GetMapping("/api/discovery/teams")
    @PreAuthorize("hasAnyRole('CLIENT', 'CONTRACTOR', 'ADMIN')")
    public Page<TeamDiscoveryResponse> searchTeams(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return discoveryService.searchTeams(location, name, pageable);
    }

    @GetMapping("/api/discovery/requirements")
    @PreAuthorize("hasAnyRole('WORKER', 'CONTRACTOR', 'CLIENT', 'ADMIN')")
    public Page<RequirementDiscoveryResponse> searchRequirements(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) WorkerType workerType,
            @RequestParam(required = false) UUID skillId,
            @RequestParam(required = false) BigDecimal minDailyRate,
            @RequestParam(required = false) BigDecimal maxDailyRate,
            @RequestParam(required = false) Boolean accommodationAvailable,
            @RequestParam(required = false) Boolean foodAvailable,
            Pageable pageable) {
        return discoveryService.searchRequirements(location, workerType, skillId, minDailyRate, maxDailyRate, accommodationAvailable, foodAvailable, pageable);
    }
}
