package com.nirmaansetu.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nirmaansetu.auth.domain.AccountStatus;
import com.nirmaansetu.auth.domain.AuthFailureException;
import com.nirmaansetu.auth.domain.AuthRole;
import com.nirmaansetu.auth.infrastructure.persistence.AppUserEntity;
import com.nirmaansetu.auth.infrastructure.persistence.AppUserRepository;
import com.nirmaansetu.auth.infrastructure.persistence.AuthOtpChallengeEntity;
import com.nirmaansetu.auth.infrastructure.persistence.AuthOtpChallengeRepository;
import com.nirmaansetu.auth.infrastructure.persistence.AuthPhoneIdentityEntity;
import com.nirmaansetu.auth.infrastructure.persistence.AuthPhoneIdentityRepository;
import com.nirmaansetu.auth.infrastructure.persistence.AuthSessionEntity;
import com.nirmaansetu.auth.infrastructure.persistence.AuthSessionRepository;
import com.nirmaansetu.auth.infrastructure.persistence.UserRoleRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {
    private final Instant now = Instant.parse("2026-08-26T10:00:00Z");
    @Mock private AuthOtpChallengeRepository challenges;
    @Mock private AuthPhoneIdentityRepository identities;
    @Mock private AppUserRepository users;
    @Mock private UserRoleRepository roles;
    @Mock private AuthSessionRepository sessions;
    @Mock private PlatformTransactionManager transactionManager;
    @Mock private TransactionStatus transactionStatus;
    private CapturingOtpDelivery delivery;
    private AuthService service;
    private PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        AuthProperties properties = new AuthProperties();
        delivery = new CapturingOtpDelivery(); encoder = new BCryptPasswordEncoder();
        service = new AuthService(properties, new PhoneNumberNormalizer(), new FixedOtpCodeGenerator(), delivery, encoder, new TokenService(),
            challenges, identities, users, roles, sessions, Clock.fixed(now, ZoneOffset.UTC), transactionManager);

        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        when(challenges.save(any(AuthOtpChallengeEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(identities.save(any(AuthPhoneIdentityEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(users.save(any(AppUserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessions.save(any(AuthSessionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void startsAuthenticationForANewPhoneWithoutLoggingTheOtp(CapturedOutput output) {
        service.startOtp("9876543210");
        ArgumentCaptor<AuthOtpChallengeEntity> saved = ArgumentCaptor.forClass(AuthOtpChallengeEntity.class);
        verify(challenges).save(saved.capture());
        assertThat(delivery.phone).isEqualTo("+919876543210");
        assertThat(saved.getValue().matches("654321", encoder)).isTrue();
        assertThat(output.getOut()).doesNotContain("654321");
    }

    @Test
    void verifiesOtpAndReturnsAllServerAssignedRoles() {
        UUID userId = UUID.randomUUID();
        AuthOtpChallengeEntity challenge = challengeFor("+919876543210", "654321", now.plusSeconds(60));
        when(challenges.findFirstByPhoneE164AndConsumedAtIsNullOrderByCreatedAtDesc("+919876543210")).thenReturn(Optional.of(challenge));
        when(identities.findByPhoneE164("+919876543210")).thenReturn(Optional.of(new AuthPhoneIdentityEntity(UUID.randomUUID(), userId, "+919876543210", now)));
        when(users.findById(userId)).thenReturn(Optional.of(new AppUserEntity(userId)));
        when(roles.findRoleCodesByUserId(userId)).thenReturn(List.of("WORKER", "CONTRACTOR"));

        AuthSessionResult result = service.verifyOtp("+91 98765-43210", "654321");

        assertThat(result.roles()).containsExactlyInAnyOrder(AuthRole.WORKER, AuthRole.CONTRACTOR);
        assertThat(result.accessToken()).isNotBlank();
        assertThat(challenge.isConsumed()).isTrue();
    }

    @Test
    void rejectsInvalidExpiredAndReusedOtps() {
        AuthOtpChallengeEntity invalid = challengeFor("+919876543210", "654321", now.plusSeconds(60));
        when(challenges.findFirstByPhoneE164AndConsumedAtIsNullOrderByCreatedAtDesc("+919876543210")).thenReturn(Optional.of(invalid));
        assertThatThrownBy(() -> service.verifyOtp("9876543210", "000000")).isInstanceOf(AuthFailureException.class);

        AuthOtpChallengeEntity expired = challengeFor("+919876543210", "654321", now.minusSeconds(1));
        when(challenges.findFirstByPhoneE164AndConsumedAtIsNullOrderByCreatedAtDesc("+919876543210")).thenReturn(Optional.of(expired));
        assertThatThrownBy(() -> service.verifyOtp("9876543210", "654321")).isInstanceOf(AuthFailureException.class);

        when(challenges.findFirstByPhoneE164AndConsumedAtIsNullOrderByCreatedAtDesc("+919876543210")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.verifyOtp("9876543210", "654321")).isInstanceOf(AuthFailureException.class);
    }

    @Test
    void blocksDisabledUsersAndRevokesSessionsOnLogout() {
        UUID userId = UUID.randomUUID(); UUID sessionId = UUID.randomUUID();
        AuthOtpChallengeEntity challenge = challengeFor("+919876543210", "654321", now.plusSeconds(60));
        when(challenges.findFirstByPhoneE164AndConsumedAtIsNullOrderByCreatedAtDesc("+919876543210")).thenReturn(Optional.of(challenge));
        when(identities.findByPhoneE164("+919876543210")).thenReturn(Optional.of(new AuthPhoneIdentityEntity(UUID.randomUUID(), userId, "+919876543210", now)));
        AppUserEntity disabled = new AppUserEntity(userId, AccountStatus.DISABLED);
        when(users.findById(userId)).thenReturn(Optional.of(disabled));
        assertThatThrownBy(() -> service.verifyOtp("9876543210", "654321")).isInstanceOf(AuthFailureException.class);

        AuthSessionEntity session = new AuthSessionEntity(sessionId, userId, "a".repeat(64), now.plusSeconds(60));
        when(sessions.findById(sessionId)).thenReturn(Optional.of(session));
        service.logout(new AuthPrincipal(userId, sessionId, java.util.Set.of()));
        assertThat(session.isUsable(now)).isFalse();
    }

    @Test
    void otpAttemptLimitExactBoundaries() {
        UUID userId = UUID.randomUUID();
        String phone = "+919876543210";
        String correctCode = "654321";
        String wrongCode = "000000";

        when(identities.findByPhoneE164(phone)).thenReturn(Optional.of(new AuthPhoneIdentityEntity(UUID.randomUUID(), userId, phone, now)));
        when(users.findById(userId)).thenReturn(Optional.of(new AppUserEntity(userId)));
        when(roles.findRoleCodesByUserId(userId)).thenReturn(List.of("WORKER"));

        AuthOtpChallengeEntity challenge = challengeFor(phone, correctCode, now.plusSeconds(60));
        when(challenges.findFirstByPhoneE164AndConsumedAtIsNullOrderByCreatedAtDesc(phone)).thenReturn(Optional.of(challenge));

        // attempts 0: wrong OTP attempt. Verification proceeds, attempts increments to 1, challenge saved, throws.
        assertThatThrownBy(() -> service.verifyOtp(phone, wrongCode)).isInstanceOf(AuthFailureException.class);
        assertThat(challenge.getAttempts()).isEqualTo(1);

        // attempts 1: wrong OTP attempt -> attempts increments to 2
        assertThatThrownBy(() -> service.verifyOtp(phone, wrongCode)).isInstanceOf(AuthFailureException.class);
        assertThat(challenge.getAttempts()).isEqualTo(2);

        // attempts 2: wrong OTP attempt -> attempts increments to 3
        assertThatThrownBy(() -> service.verifyOtp(phone, wrongCode)).isInstanceOf(AuthFailureException.class);
        assertThat(challenge.getAttempts()).isEqualTo(3);

        // attempts 3: wrong OTP attempt -> attempts increments to 4
        assertThatThrownBy(() -> service.verifyOtp(phone, wrongCode)).isInstanceOf(AuthFailureException.class);
        assertThat(challenge.getAttempts()).isEqualTo(4);

        // attempts 4: wrong OTP attempt -> attempts increments to 5 (the fifth failed attempt)
        assertThatThrownBy(() -> service.verifyOtp(phone, wrongCode)).isInstanceOf(AuthFailureException.class);
        assertThat(challenge.getAttempts()).isEqualTo(5);

        // Now, attempts = 5. The limit is reached.
        // attempts 5: correct OTP after the attempt limit must fail and attempts must not increase further.
        assertThatThrownBy(() -> service.verifyOtp(phone, correctCode)).isInstanceOf(AuthFailureException.class);
        assertThat(challenge.getAttempts()).isEqualTo(5); // Still 5

        // attempts 5: incorrect OTP after the attempt limit must fail and attempts must not increase further.
        assertThatThrownBy(() -> service.verifyOtp(phone, wrongCode)).isInstanceOf(AuthFailureException.class);
        assertThat(challenge.getAttempts()).isEqualTo(5); // Still 5
    }

    @Test
    void succeedsOnFifthAttemptWithCorrectOtp() {
        UUID userId = UUID.randomUUID();
        String phone = "+919876543210";
        String correctCode = "654321";

        when(identities.findByPhoneE164(phone)).thenReturn(Optional.of(new AuthPhoneIdentityEntity(UUID.randomUUID(), userId, phone, now)));
        when(users.findById(userId)).thenReturn(Optional.of(new AppUserEntity(userId)));
        when(roles.findRoleCodesByUserId(userId)).thenReturn(List.of("WORKER"));

        AuthOtpChallengeEntity challenge = challengeFor(phone, correctCode, now.plusSeconds(60));
        for (int i = 0; i < 4; i++) {
            challenge.registerFailedAttempt();
        }
        assertThat(challenge.getAttempts()).isEqualTo(4);

        when(challenges.findFirstByPhoneE164AndConsumedAtIsNullOrderByCreatedAtDesc(phone)).thenReturn(Optional.of(challenge));

        AuthSessionResult result = service.verifyOtp(phone, correctCode);
        assertThat(result.accessToken()).isNotBlank();
        assertThat(challenge.isConsumed()).isTrue();
        assertThat(challenge.getAttempts()).isEqualTo(4);
    }

    @Test
    void authenticatesValidSessionCorrect() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String token = "my-valid-token";
        String tokenHash = new TokenService().hash(token);
        
        AuthSessionEntity session = new AuthSessionEntity(sessionId, userId, tokenHash, now.plusSeconds(3600));
        when(sessions.findByTokenHash(tokenHash)).thenReturn(Optional.of(session));
        
        AppUserEntity user = new AppUserEntity(userId, AccountStatus.ACTIVE);
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(roles.findRoleCodesByUserId(userId)).thenReturn(List.of("WORKER"));

        AuthPrincipal principal = service.authenticate(token);
        assertThat(principal).isNotNull();
        assertThat(principal.userId()).isEqualTo(userId);
        assertThat(principal.sessionId()).isEqualTo(sessionId);
        assertThat(principal.roles()).containsExactly(AuthRole.WORKER);
    }

    @Test
    void rejectsExpiredSession() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String token = "expired-token";
        String tokenHash = new TokenService().hash(token);
        
        AuthSessionEntity session = new AuthSessionEntity(sessionId, userId, tokenHash, now.minusSeconds(1));
        when(sessions.findByTokenHash(tokenHash)).thenReturn(Optional.of(session));

        AuthPrincipal principal = service.authenticate(token);
        assertThat(principal).isNull();
    }

    @Test
    void rejectsRevokedSession() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String token = "revoked-token";
        String tokenHash = new TokenService().hash(token);
        
        AuthSessionEntity session = new AuthSessionEntity(sessionId, userId, tokenHash, now.plusSeconds(3600));
        session.revoke(now);
        when(sessions.findByTokenHash(tokenHash)).thenReturn(Optional.of(session));

        AuthPrincipal principal = service.authenticate(token);
        assertThat(principal).isNull();
    }

    @Test
    void rejectsInvalidToken() {
        String token = "invalid-token";
        String tokenHash = new TokenService().hash(token);
        when(sessions.findByTokenHash(tokenHash)).thenReturn(Optional.empty());

        AuthPrincipal principal = service.authenticate(token);
        assertThat(principal).isNull();
    }

    @Test
    void rejectsDisabledUserForSession() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String token = "disabled-user-token";
        String tokenHash = new TokenService().hash(token);
        
        AuthSessionEntity session = new AuthSessionEntity(sessionId, userId, tokenHash, now.plusSeconds(3600));
        when(sessions.findByTokenHash(tokenHash)).thenReturn(Optional.of(session));
        
        AppUserEntity user = new AppUserEntity(userId, AccountStatus.DISABLED);
        when(users.findById(userId)).thenReturn(Optional.of(user));

        AuthPrincipal principal = service.authenticate(token);
        assertThat(principal).isNull();
    }

    @Test
    void verifiesTransactionRollbackOnUniqueConstraintFailure() {
        String phone = "+919876543210";
        String code = "654321";

        AuthOtpChallengeEntity challenge = challengeFor(phone, code, now.plusSeconds(60));
        when(challenges.findFirstByPhoneE164AndConsumedAtIsNullOrderByCreatedAtDesc(phone)).thenReturn(Optional.of(challenge));

        when(identities.findByPhoneE164(phone)).thenReturn(Optional.empty());
        when(users.save(any(AppUserEntity.class))).thenThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate key value violates unique constraint"));

        assertThatThrownBy(() -> service.verifyOtp(phone, code))
            .isInstanceOf(AuthFailureException.class)
            .hasMessage("Authentication could not be completed.");

        verify(transactionManager).rollback(any());
    }

    private AuthOtpChallengeEntity challengeFor(String phone, String code, Instant expiresAt) {
        return new AuthOtpChallengeEntity(UUID.randomUUID(), phone, encoder.encode(code), expiresAt, now);
    }
    private static class FixedOtpCodeGenerator extends OtpCodeGenerator { @Override public String generate() { return "654321"; } }
    private static class CapturingOtpDelivery implements OtpDelivery { String phone; @Override public void deliver(String phoneE164, String code) { phone = phoneE164; } }
}
