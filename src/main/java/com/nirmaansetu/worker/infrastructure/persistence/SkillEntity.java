package com.nirmaansetu.worker.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "skill")
public class SkillEntity {
    @Id private UUID id;
    @Column(name = "code") private String code;
    @Column(name = "name") private String name;
    @Column(name = "is_active") private boolean isActive;

    protected SkillEntity() {}

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public boolean isActive() { return isActive; }
}
