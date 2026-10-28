package com.nirmaansetu.auth.application;

import com.nirmaansetu.auth.domain.OtpDeliveryUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
@Profile("!test")
public class UnavailableOtpDelivery implements OtpDelivery {
    private static final Logger log = LoggerFactory.getLogger(UnavailableOtpDelivery.class);

    private final boolean devMode;
    private final String provider;
    private final String apiKey;
    private final String senderId;
    private final String templateId;
    private final HttpClient httpClient;

    public UnavailableOtpDelivery(
            @Value("${auth.otp.dev-mode:true}") boolean devMode,
            @Value("${nirmaansetu.sms.provider:dev}") String provider,
            @Value("${nirmaansetu.sms.api-key:}") String apiKey,
            @Value("${nirmaansetu.sms.sender-id:}") String senderId,
            @Value("${nirmaansetu.sms.template-id:}") String templateId) {
        this.devMode = devMode;
        this.provider = provider != null ? provider.trim().toLowerCase() : "dev";
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.senderId = senderId != null ? senderId.trim() : "";
        this.templateId = templateId != null ? templateId.trim() : "";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public void deliver(String phoneE164, String code) {
        if (devMode || "dev".equals(provider)) {
            log.info("\n========================================\n[DEV-ONLY] OTP CODE FOR {}: {}\n========================================\n", phoneE164, code);
            return;
        }

        if (apiKey.isBlank()) {
            log.error("SMS delivery failed: No API key configured for provider '{}'", provider);
            throw new OtpDeliveryUnavailableException();
        }

        try {
            switch (provider) {
                case "fast2sms" -> sendFast2Sms(phoneE164, code);
                case "msg91" -> sendMsg91(phoneE164, code);
                case "twilio" -> sendTwilio(phoneE164, code);
                default -> {
                    log.error("Unsupported SMS provider: '{}'", provider);
                    throw new OtpDeliveryUnavailableException();
                }
            }
        } catch (OtpDeliveryUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to send OTP to {} via provider '{}': {}", phoneE164, provider, e.getMessage(), e);
            throw new OtpDeliveryUnavailableException();
        }
    }

    private void sendFast2Sms(String phoneE164, String code) throws Exception {
        // Fast2SMS expects 10-digit Indian phone number without +91
        String nationalNumber = phoneE164.replaceAll("^\\+91", "").replaceAll("[^0-9]", "");
        String jsonPayload = String.format("{\"variables_values\":\"%s\",\"route\":\"otp\",\"numbers\":\"%s\"}", code, nationalNumber);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.fast2sms.com/dev/bulkV2"))
                .header("authorization", apiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            log.error("Fast2SMS gateway returned error: HTTP {} body: {}", response.statusCode(), response.body());
            throw new OtpDeliveryUnavailableException();
        }
        log.info("Successfully dispatched OTP via Fast2SMS to national number suffix ***{}", nationalNumber.length() >= 4 ? nationalNumber.substring(nationalNumber.length() - 4) : "xxxx");
    }

    private void sendMsg91(String phoneE164, String code) throws Exception {
        String cleanPhone = phoneE164.replaceAll("[^0-9]", "");
        String url = String.format("https://control.msg91.com/api/v5/otp?template_id=%s&mobile=%s&authkey=%s&otp=%s",
                templateId, cleanPhone, apiKey, code);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            log.error("MSG91 gateway returned error: HTTP {} body: {}", response.statusCode(), response.body());
            throw new OtpDeliveryUnavailableException();
        }
        log.info("Successfully dispatched OTP via MSG91");
    }

    private void sendTwilio(String phoneE164, String code) throws Exception {
        // Twilio requires basic auth with Account SID and Auth Token, and sender phone number or messaging service SID
        // senderId acts as the Twilio From number or messaging service SID
        String body = "From=" + java.net.URLEncoder.encode(senderId, java.nio.charset.StandardCharsets.UTF_8)
                + "&To=" + java.net.URLEncoder.encode(phoneE164, java.nio.charset.StandardCharsets.UTF_8)
                + "&Body=" + java.net.URLEncoder.encode("Your NirmaanSetu verification code is: " + code, java.nio.charset.StandardCharsets.UTF_8);

        // Twilio API URL format: https://api.twilio.com/2010-04-01/Accounts/{AccountSid}/Messages.json
        // apiKey formatted as "AccountSid:AuthToken"
        String[] creds = apiKey.split(":", 2);
        if (creds.length < 2) {
            throw new IllegalArgumentException("Twilio API key must be in format 'AccountSid:AuthToken'");
        }
        String accountSid = creds[0];
        String basicAuth = java.util.Base64.getEncoder().encodeToString(apiKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + basicAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            log.error("Twilio gateway returned error: HTTP {} body: {}", response.statusCode(), response.body());
            throw new OtpDeliveryUnavailableException();
        }
        log.info("Successfully dispatched OTP via Twilio");
    }
}
