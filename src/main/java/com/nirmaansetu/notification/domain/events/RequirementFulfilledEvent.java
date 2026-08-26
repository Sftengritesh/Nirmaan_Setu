package com.nirmaansetu.notification.domain.events;

import java.util.UUID;

public record RequirementFulfilledEvent(
    UUID requirementId,
    UUID recipientUserId
) {}
