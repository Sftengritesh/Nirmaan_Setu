package com.nirmaansetu.operations.dpr;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DprService {

    private final DprRepository dprRepository;

    public DprService(DprRepository dprRepository) {
        this.dprRepository = dprRepository;
    }

    @Transactional
    public DprResponse createDpr(UUID userId, CreateDprRequest req) {
        DprEntity entity = new DprEntity(
            UUID.randomUUID(),
            req.projectId(),
            req.projectTitle() != null ? req.projectTitle() : "Construction Project",
            req.siteId(),
            req.siteName() != null ? req.siteName() : "Main Worksite",
            req.reportDate(),
            req.reportedBy() != null ? req.reportedBy() : "Site Supervisor",
            userId,
            req.weatherCondition(),
            req.skilledLaborCount(),
            req.unskilledLaborCount(),
            req.tasksCompleted(),
            req.delaysOrIssues(),
            req.photos() != null ? req.photos() : "[]"
        );

        if (req.safetyObservations() != null) {
            entity.setSafetyObservations(req.safetyObservations());
        }

        return DprResponse.from(dprRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<DprResponse> getDprs(UUID projectId) {
        if (projectId != null) {
            return dprRepository.findByProjectIdOrderByReportDateDesc(projectId)
                .stream().map(DprResponse::from).toList();
        }
        return dprRepository.findAllByOrderByReportDateDesc()
            .stream().map(DprResponse::from).toList();
    }
}
