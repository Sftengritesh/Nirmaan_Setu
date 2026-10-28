package com.nirmaansetu.operations.milestone;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

record CreateMilestoneRequest(
    @NotNull UUID projectId,
    @NotBlank String phaseName,
    String description,
    @NotNull LocalDate targetDate,
    Integer orderIndex
) {}

record UpdateMilestoneStatusRequest(
    @NotBlank String status
) {}

record MilestoneResponse(
    UUID id,
    UUID projectId,
    String phaseName,
    String description,
    LocalDate targetDate,
    LocalDate completionDate,
    String status,
    int orderIndex
) {
    public static MilestoneResponse from(MilestoneEntity m) {
        return new MilestoneResponse(
            m.getId(), m.getProjectId(), m.getPhaseName(), m.getDescription(),
            m.getTargetDate(), m.getCompletionDate(), m.getStatus(), m.getOrderIndex()
        );
    }
}
