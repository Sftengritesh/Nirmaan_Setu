package com.nirmaansetu.operations.material;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

record CreateMaterialRequest(
    UUID projectId,
    String projectTitle,
    UUID siteId,
    @NotBlank String itemName,
    @NotBlank String category,
    @NotNull BigDecimal quantity,
    @NotBlank String unit,
    BigDecimal minThreshold,
    @NotNull BigDecimal unitCost,
    String supplierName
) {}

record UpdateStockRequest(
    @NotNull BigDecimal newQuantity
) {}

record MaterialResponse(
    UUID id,
    UUID projectId,
    String projectTitle,
    UUID siteId,
    String itemName,
    String category,
    BigDecimal quantity,
    String unit,
    BigDecimal minThreshold,
    BigDecimal unitCost,
    String supplierName,
    String status,
    Instant lastRestockedAt,
    Instant createdAt
) {
    public static MaterialResponse from(MaterialEntity m) {
        return new MaterialResponse(
            m.getId(), m.getProjectId(), m.getProjectTitle(), m.getSiteId(),
            m.getItemName(), m.getCategory(), m.getQuantity(), m.getUnit(),
            m.getMinThreshold(), m.getUnitCost(), m.getSupplierName(), m.getStatus(),
            m.getLastRestockedAt(), m.getCreatedAt()
        );
    }
}
