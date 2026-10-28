package com.nirmaansetu.operations.site;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SiteService {
    private final SiteRepository siteRepository;

    public SiteService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Transactional
    public SiteResponse createSite(CreateSiteRequest req) {
        SiteEntity entity = new SiteEntity(
            UUID.randomUUID(),
            req.projectId(),
            req.name(),
            req.projectTitle() != null ? req.projectTitle() : "Main Site",
            req.address(),
            req.city(),
            req.state(),
            req.latitude(),
            req.longitude(),
            req.geofenceRadiusMeters() != null ? req.geofenceRadiusMeters() : 150,
            req.managerName(),
            req.contactPhone()
        );
        SiteEntity saved = siteRepository.save(entity);
        return SiteResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<SiteResponse> getAllSites() {
        return siteRepository.findAll().stream().map(SiteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SiteResponse> getSitesByProject(UUID projectId) {
        return siteRepository.findByProjectId(projectId).stream().map(SiteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SiteResponse getSiteById(UUID id) {
        return siteRepository.findById(id).map(SiteResponse::from)
            .orElseThrow(() -> new IllegalArgumentException("Site not found: " + id));
    }

    @Transactional
    public void deleteSite(UUID id) {
        siteRepository.deleteById(id);
    }
}
