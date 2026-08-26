package com.nirmaansetu.client.application;

import com.nirmaansetu.client.api.ClientProfileResponse;
import com.nirmaansetu.client.api.CreateClientProfileRequest;
import com.nirmaansetu.client.api.UpdateClientProfileRequest;
import com.nirmaansetu.client.domain.ClientProfileException;
import com.nirmaansetu.client.domain.ClientType;
import com.nirmaansetu.client.infrastructure.persistence.ClientProfileEntity;
import com.nirmaansetu.client.infrastructure.persistence.ClientProfileRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientService {

    private final ClientProfileRepository clientProfileRepository;
    private final Clock clock;

    public ClientService(ClientProfileRepository clientProfileRepository, Clock clock) {
        this.clientProfileRepository = clientProfileRepository;
        this.clock = clock;
    }

    @Transactional
    public ClientProfileResponse createProfile(UUID userId, CreateClientProfileRequest request) {
        if (clientProfileRepository.existsByUserId(userId)) {
            throw new ClientProfileException("A client profile already exists for this account.");
        }

        ClientType type = parseClientType(request.clientType());
        Instant now = clock.instant();
        ClientProfileEntity entity = new ClientProfileEntity(
            UUID.randomUUID(), userId, type, request.displayName().trim(), now
        );

        try {
            entity = clientProfileRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new ClientProfileException("A client profile already exists for this account.");
        }

        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public ClientProfileResponse getProfile(UUID userId) {
        ClientProfileEntity entity = clientProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ClientProfileException("Client profile not found."));
        return toResponse(entity);
    }

    @Transactional
    public ClientProfileResponse updateProfile(UUID userId, UpdateClientProfileRequest request) {
        ClientProfileEntity entity = clientProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ClientProfileException("Client profile not found."));

        ClientType type = parseClientType(request.clientType());
        entity.setClientType(type);
        entity.setDisplayName(request.displayName().trim());
        entity.setUpdatedAt(clock.instant());

        return toResponse(clientProfileRepository.save(entity));
    }

    private ClientType parseClientType(String raw) {
        try {
            return ClientType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ClientProfileException("Invalid client type. Allowed values: HOMEOWNER, BUILDER, BUSINESS, OTHER.");
        }
    }

    private ClientProfileResponse toResponse(ClientProfileEntity entity) {
        return new ClientProfileResponse(
            entity.getId(),
            entity.getUserId(),
            entity.getClientType().name(),
            entity.getDisplayName(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
