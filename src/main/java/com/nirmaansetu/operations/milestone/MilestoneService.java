package com.nirmaansetu.operations.milestone;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;

    public MilestoneService(MilestoneRepository milestoneRepository) {
        this.milestoneRepository = milestoneRepository;
    }

    @Transactional(readOnly = true)
    public List<MilestoneResponse> getMilestonesByProject(UUID projectId) {
        return milestoneRepository.findByProjectIdOrderByOrderIndexAsc(projectId)
            .stream().map(MilestoneResponse::from).toList();
    }

    @Transactional
    public MilestoneResponse createMilestone(CreateMilestoneRequest req) {
        int order = req.orderIndex() != null ? req.orderIndex() : 1;
        MilestoneEntity m = new MilestoneEntity(
            UUID.randomUUID(), req.projectId(), req.phaseName(), req.description(), req.targetDate(), order
        );
        return MilestoneResponse.from(milestoneRepository.save(m));
    }

    @Transactional
    public MilestoneResponse updateMilestoneStatus(UUID milestoneId, String status) {
        MilestoneEntity m = milestoneRepository.findById(milestoneId)
            .orElseThrow(() -> new IllegalArgumentException("Milestone not found: " + milestoneId));
        m.setStatus(status.toUpperCase().trim());
        if ("COMPLETED".equalsIgnoreCase(status)) {
            m.setCompletionDate(LocalDate.now());
        }
        m.setUpdatedAt(Instant.now());
        return MilestoneResponse.from(milestoneRepository.save(m));
    }

    @Transactional
    public void createDefaultMilestonesForProject(UUID projectId, LocalDate estimatedEndDate) {
        String[][] defaultPhases = {
            {"1. Site Excavation & Clearing", "Site layout, ground levelling, deep trenching"},
            {"2. Foundation & Substructure", "PCC, Footing reinforcement, plinth beam casting"},
            {"3. RCC Superstructure", "Columns, beams, and slab formwork & casting"},
            {"4. Brick Masonry & Plastering", "Exterior and partition brickwork with internal plaster"},
            {"5. MEP & Electrical Conduit", "Plumbing lines, sanitary pipes, concealed conduits"},
            {"6. Finishes, Tile & Painting", "Vitrified tiles, primer and 2-coat emulsion painting"},
            {"7. Final Inspection & Handover", "Snag list clearance and formal handover to client"}
        };

        for (int i = 0; i < defaultPhases.length; i++) {
            MilestoneEntity m = new MilestoneEntity(
                UUID.randomUUID(), projectId, defaultPhases[i][0], defaultPhases[i][1], estimatedEndDate, i + 1
            );
            if (i == 0) m.setStatus("IN_PROGRESS");
            milestoneRepository.save(m);
        }
    }
}
