package com.nirmaansetu.operations.material;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Transactional
    public MaterialResponse createMaterial(CreateMaterialRequest req) {
        MaterialEntity entity = new MaterialEntity(
            UUID.randomUUID(),
            req.projectId(),
            req.projectTitle() != null ? req.projectTitle() : "Main Site",
            req.siteId(),
            req.itemName(),
            req.category(),
            req.quantity(),
            req.unit(),
            req.minThreshold() != null ? req.minThreshold() : BigDecimal.valueOf(10),
            req.unitCost(),
            req.supplierName() != null ? req.supplierName() : "General Supplier"
        );
        return MaterialResponse.from(materialRepository.save(entity));
    }

    @Transactional
    public MaterialResponse updateStock(UUID id, BigDecimal newQuantity) {
        MaterialEntity entity = materialRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Material not found: " + id));

        BigDecimal safeQty = newQuantity.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newQuantity;
        entity.setQuantity(safeQty);
        entity.setStatus(MaterialEntity.computeStatus(safeQty, entity.getMinThreshold()));
        entity.setLastRestockedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        return MaterialResponse.from(materialRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> getMaterials(UUID projectId) {
        if (projectId != null) {
            return materialRepository.findByProjectIdOrderByItemNameAsc(projectId)
                .stream().map(MaterialResponse::from).toList();
        }
        return materialRepository.findAllByOrderByItemNameAsc()
            .stream().map(MaterialResponse::from).toList();
    }

    @Transactional
    public void deleteMaterial(UUID id) {
        materialRepository.deleteById(id);
    }
}
