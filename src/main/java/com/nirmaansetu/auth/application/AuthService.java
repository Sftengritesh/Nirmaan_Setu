package com.nirmaansetu.auth.application;

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
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class AuthService {
    private final AuthProperties properties;
    private final PhoneNumberNormalizer phoneNumberNormalizer;
    private final OtpCodeGenerator otpCodeGenerator;
    private final OtpDelivery otpDelivery;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthOtpChallengeRepository challengeRepository;
    private final AuthPhoneIdentityRepository phoneIdentityRepository;
    private final AppUserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthSessionRepository sessionRepository;
    private final Clock clock;
    private final TransactionTemplate transactionTemplate;

    public AuthService(AuthProperties properties, PhoneNumberNormalizer phoneNumberNormalizer, OtpCodeGenerator otpCodeGenerator,
                       OtpDelivery otpDelivery, PasswordEncoder passwordEncoder, TokenService tokenService,
                       AuthOtpChallengeRepository challengeRepository, AuthPhoneIdentityRepository phoneIdentityRepository,
                       AppUserRepository userRepository, UserRoleRepository userRoleRepository,
                       AuthSessionRepository sessionRepository, Clock clock, PlatformTransactionManager transactionManager) {
        this.properties = properties; this.phoneNumberNormalizer = phoneNumberNormalizer; this.otpCodeGenerator = otpCodeGenerator;
        this.otpDelivery = otpDelivery; this.passwordEncoder = passwordEncoder; this.tokenService = tokenService;
        this.challengeRepository = challengeRepository; this.phoneIdentityRepository = phoneIdentityRepository;
        this.userRepository = userRepository; this.userRoleRepository = userRoleRepository; this.sessionRepository = sessionRepository;
        this.clock = clock;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * Initiates an OTP verification flow.
     * 
     * WARNING: Production deployment must introduce a request rate limiting boundary (e.g. at the API Gateway or 
     * web filter layer) to prevent OTP/SMS start abuse before enabling any paid SMS provider.
     * The current implementation deletes expired OTP challenges during generation but does NOT provide 
     * production-grade rate limiting.
     */
    @Transactional
    public void startOtp(String rawPhoneNumber) {
        String phone = phoneNumberNormalizer.normalizeIndian(rawPhoneNumber);
        Instant now = clock.instant();
        challengeRepository.deleteByCreatedAtBefore(now.minus(properties.getOtp().getRetention()));
        String code = otpCodeGenerator.generate();
        challengeRepository.save(new AuthOtpChallengeEntity(UUID.randomUUID(), phone, passwordEncoder.encode(code), now.plus(properties.getOtp().getTtl()), now));
        otpDelivery.deliver(phone, code);
    }

    /**
     * Verifies the provided OTP code against the latest challenge for the normalized phone number.
     * 
     * Split into two distinct database transactions:
     * 1. OTP Verification: retrieves challenge with pessimistic write lock, checks attempt limits/codes,
     *    increments attempts on mismatch, and commits immediately to safely persist attempts.
     * 2. Identity & Session Setup: checks/registers phone identity, retrieves user account status,
     *    and saves the user session token hash. Any uniqueness/database failures here will roll back
     *    completely, preventing partial authentication states.
     * 
     * Note: A newly created user starts with zero roles. No roles (CLIENT, WORKER, CONTRACTOR, ADMIN) are
     * assigned automatically during registration. Role information is never trusted from the client.
     */
    public AuthSessionResult verifyOtp(String rawPhoneNumber, String code) {
        String phone = phoneNumberNormalizer.normalizeIndian(rawPhoneNumber);
        Instant now = clock.instant();

        AuthOtpChallengeEntity challenge;
        try {
            challenge = transactionTemplate.execute(status -> {
                AuthOtpChallengeEntity chal = challengeRepository.findFirstByPhoneE164AndConsumedAtIsNullOrderByCreatedAtDesc(phone)
                    .orElseThrow(() -> invalidOtp());
                if (chal.isExpired(now) || chal.getAttempts() >= properties.getOtp().getMaxAttempts()) {
                    throw invalidOtp();
                }
                if (!chal.matches(code, passwordEncoder)) {
                    chal.registerFailedAttempt();
                    return challengeRepository.save(chal);
                }
                chal.consume(now);
                return challengeRepository.save(chal);
            });
        } catch (AuthFailureException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AuthFailureException("Authentication could not be completed.");
        }

        if (challenge == null || !challenge.isConsumed()) {
            throw invalidOtp();
        }

        try {
            return transactionTemplate.execute(status -> {
                AuthPhoneIdentityEntity identity = phoneIdentityRepository.findByPhoneE164(phone)
                    .orElseGet(() -> createIdentity(phone, now));
                AppUserEntity user = userRepository.findById(identity.getUserId())
                    .orElseThrow(() -> new AuthFailureException("Authentication could not be completed."));
                if (user.getAccountStatus() != AccountStatus.ACTIVE) {
                    throw new AuthFailureException("Authentication could not be completed.");
                }
                return createSession(user.getId(), rolesFor(user.getId()), now);
            });
        } catch (AuthFailureException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AuthFailureException("Authentication could not be completed.");
        }
    }

    @Transactional
    public void logout(AuthPrincipal principal) {
        sessionRepository.findById(principal.sessionId()).ifPresent(session -> {
            session.revoke(clock.instant());
            sessionRepository.save(session);
        });
    }

    public AuthPrincipal authenticate(String token) {
        Instant now = clock.instant();
        return sessionRepository.findByTokenHash(tokenService.hash(token))
            .filter(session -> session.isUsable(now))
            .flatMap(session -> userRepository.findById(session.getUserId()).filter(user -> user.getAccountStatus() == AccountStatus.ACTIVE)
                .map(user -> new AuthPrincipal(user.getId(), session.getId(), rolesFor(user.getId()))))
            .orElse(null);
    }

    private AuthPhoneIdentityEntity createIdentity(String phone, Instant now) {
        AppUserEntity user = userRepository.save(new AppUserEntity(UUID.randomUUID()));
        AuthPhoneIdentityEntity identity = new AuthPhoneIdentityEntity(UUID.randomUUID(), user.getId(), phone, now);
        return phoneIdentityRepository.save(identity);
    }

    private AuthSessionResult createSession(UUID userId, EnumSet<AuthRole> roles, Instant now) {
        String token = tokenService.createToken();
        Instant expiresAt = now.plus(properties.getSession().getTtl());
        sessionRepository.save(new AuthSessionEntity(UUID.randomUUID(), userId, tokenService.hash(token), expiresAt));
        return new AuthSessionResult(userId, roles, token, expiresAt);
    }

    private EnumSet<AuthRole> rolesFor(UUID userId) {
        List<String> codes = userRoleRepository.findRoleCodesByUserId(userId);
        EnumSet<AuthRole> roles = EnumSet.noneOf(AuthRole.class);
        for (String code : codes) roles.add(AuthRole.valueOf(code));
        return roles;
    }

    private AuthFailureException invalidOtp() { return new AuthFailureException("Invalid or expired verification code."); }
}
