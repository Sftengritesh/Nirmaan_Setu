package com.nirmaansetu.operations.material;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "material")
public class MaterialEntity {

    @Id
    private UUID id;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "project_title", length = 200)
    private String projectTitle;

    @Column(name = "site_id")
    private UUID siteId;

    @Column(name = "item_name", nullable = false, length = 120)
    private String itemName;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false, length = 30)
    private String unit;

    @Column(name = "min_threshold", nullable = false, precision = 12, scale = 2)
    private BigDecimal minThreshold = BigDecimal.valueOf(10);

    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "supplier_name", length = 160)
    private String supplierName;

    @Column(name = "supplier_contact", length = 40)
    private String supplierContact;

    @Column(nullable = false, length = 20)
    private String status = "IN_STOCK"; // IN_STOCK, LOW_STOCK, OUT_OF_STOCK

    @Column(name = "last_restocked_at", nullable = false)
    private Instant lastRestockedAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public MaterialEntity() {}

    public MaterialEntity(UUID id, UUID projectId, String projectTitle, UUID siteId,
                          String itemName, String category, BigDecimal quantity, String unit,
                          BigDecimal minThreshold, BigDecimal unitCost, String supplierName) {
        this.id = id;
        this.projectId = projectId;
        this.projectTitle = projectTitle;
        this.siteId = siteId;
        this.itemName = itemName;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.minThreshold = minThreshold != null ? minThreshold : BigDecimal.valueOf(10);
        this.unitCost = unitCost;
        this.supplierName = supplierName;
        this.status = computeStatus(quantity, this.minThreshold);
        this.lastRestockedAt = Instant.now();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static String computeStatus(BigDecimal qty, BigDecimal threshold) {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) return "OUT_OF_STOCK";
        if (qty.compareTo(threshold) <= 0) return "LOW_STOCK";
        return "IN_STOCK";
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }
    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }
    public UUID getSiteId() { return siteId; }
    public void setSiteId(UUID siteId) { this.siteId = siteId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getMinThreshold() { return minThreshold; }
    public void setMinThreshold(BigDecimal minThreshold) { this.minThreshold = minThreshold; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getSupplierContact() { return supplierContact; }
    public void setSupplierContact(String supplierContact) { this.supplierContact = supplierContact; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getLastRestockedAt() { return lastRestockedAt; }
    public void setLastRestockedAt(Instant lastRestockedAt) { this.lastRestockedAt = lastRestockedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
