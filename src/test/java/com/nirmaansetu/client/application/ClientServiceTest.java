package com.nirmaansetu.client.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nirmaansetu.client.api.ClientProfileResponse;
import com.nirmaansetu.client.api.CreateClientProfileRequest;
import com.nirmaansetu.client.api.UpdateClientProfileRequest;
import com.nirmaansetu.client.domain.ClientProfileException;
import com.nirmaansetu.client.domain.ClientType;
import com.nirmaansetu.client.infrastructure.persistence.ClientProfileEntity;
import com.nirmaansetu.client.infrastructure.persistence.ClientProfileRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClientServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-26T10:00:00Z");

    @Mock
    private ClientProfileRepository clientProfileRepository;

    private ClientService clientService;

    @BeforeEach
    void setUp() {
        clientService = new ClientService(clientProfileRepository, Clock.fixed(NOW, ZoneOffset.UTC));
        when(clientProfileRepository.save(any(ClientProfileEntity.class))).thenAnswer(i -> i.getArgument(0));
    }

    private ClientProfileEntity profileFor(UUID userId) {
        return new ClientProfileEntity(UUID.randomUUID(), userId, ClientType.HOMEOWNER, "Jane Doe", NOW);
    }

    @Test
    void createProfileSuccess() {
        UUID userId = UUID.randomUUID();
        CreateClientProfileRequest request = new CreateClientProfileRequest("HOMEOWNER", "Jane Doe");
        when(clientProfileRepository.existsByUserId(userId)).thenReturn(false);

        ClientProfileResponse response = clientService.createProfile(userId, request);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.clientType()).isEqualTo("HOMEOWNER");
        assertThat(response.displayName()).isEqualTo("Jane Doe");
    }

    @Test
    void createProfileFailsWhenDuplicateExistsInApp() {
        UUID userId = UUID.randomUUID();
        CreateClientProfileRequest request = new CreateClientProfileRequest("HOMEOWNER", "Jane Doe");
        when(clientProfileRepository.existsByUserId(userId)).thenReturn(true);

        assertThatThrownBy(() -> clientService.createProfile(userId, request))
            .isInstanceOf(ClientProfileException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    void createProfileFailsWhenDataIntegrityViolationOccurs() {
        UUID userId = UUID.randomUUID();
        CreateClientProfileRequest request = new CreateClientProfileRequest("HOMEOWNER", "Jane Doe");
        when(clientProfileRepository.existsByUserId(userId)).thenReturn(false);
        when(clientProfileRepository.save(any())).thenThrow(new DataIntegrityViolationException("Unique constraint"));

        assertThatThrownBy(() -> clientService.createProfile(userId, request))
            .isInstanceOf(ClientProfileException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    void createProfileFailsForInvalidClientType() {
        UUID userId = UUID.randomUUID();
        CreateClientProfileRequest request = new CreateClientProfileRequest("INVALID_TYPE", "Jane Doe");
        when(clientProfileRepository.existsByUserId(userId)).thenReturn(false);

        assertThatThrownBy(() -> clientService.createProfile(userId, request))
            .isInstanceOf(ClientProfileException.class)
            .hasMessageContaining("Invalid client type");
    }

    @Test
    void getProfileSuccess() {
        UUID userId = UUID.randomUUID();
        ClientProfileEntity entity = profileFor(userId);
        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(entity));

        ClientProfileResponse response = clientService.getProfile(userId);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.displayName()).isEqualTo("Jane Doe");
    }

    @Test
    void getProfileFailsWhenNotFound() {
        UUID userId = UUID.randomUUID();
        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getProfile(userId))
            .isInstanceOf(ClientProfileException.class)
            .hasMessageContaining("Client profile not found");
    }

    @Test
    void updateProfileSuccess() {
        UUID userId = UUID.randomUUID();
        ClientProfileEntity entity = profileFor(userId);
        when(clientProfileRepository.findByUserId(userId)).thenReturn(Optional.of(entity));

        UpdateClientProfileRequest request = new UpdateClientProfileRequest("BUILDER", "Jane Doe Renovations");
        ClientProfileResponse response = clientService.updateProfile(userId, request);

        assertThat(response.clientType()).isEqualTo("BUILDER");
        assertThat(response.displayName()).isEqualTo("Jane Doe Renovations");
    }
}
