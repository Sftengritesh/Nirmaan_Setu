package com.nirmaansetu.worker.api;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddSkillRequest(@NotNull UUID skillId) {}
