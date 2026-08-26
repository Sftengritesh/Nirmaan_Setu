package com.nirmaansetu.events;

import java.util.UUID;

public record RequirementFulfilledEvent(
    UUID requirementId,
    UUID recipientUserId
) {}
