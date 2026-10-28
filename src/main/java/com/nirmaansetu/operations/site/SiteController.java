package com.nirmaansetu.operations.site;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sites")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'CONTRACTOR')")
    public ResponseEntity<SiteResponse> createSite(@Valid @RequestBody CreateSiteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(siteService.createSite(request));
    }

    @GetMapping
    public List<SiteResponse> listSites(@RequestParam(required = false) UUID projectId) {
        if (projectId != null) {
            return siteService.getSitesByProject(projectId);
        }
        return siteService.getAllSites();
    }

    @GetMapping("/{id}")
    public SiteResponse getSite(@PathVariable UUID id) {
        return siteService.getSiteById(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSite(@PathVariable UUID id) {
        siteService.deleteSite(id);
        return ResponseEntity.noContent().build();
    }
}
