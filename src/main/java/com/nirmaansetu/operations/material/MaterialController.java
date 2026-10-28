package com.nirmaansetu.operations.material;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/materials")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTRACTOR')")
    public ResponseEntity<MaterialResponse> createMaterial(@Valid @RequestBody CreateMaterialRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.createMaterial(request));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTRACTOR')")
    public MaterialResponse updateStock(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStockRequest request) {
        return materialService.updateStock(id, request.newQuantity());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTRACTOR', 'CLIENT')")
    public List<MaterialResponse> getMaterials(@RequestParam(required = false) UUID projectId) {
        return materialService.getMaterials(projectId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMaterial(@PathVariable UUID id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }
}
