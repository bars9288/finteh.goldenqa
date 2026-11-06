package ru.cqa.model.execution;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record ScenarioExecutionRequest(
        @NotBlank String version,
        @NotBlank String environmentId,
        Map<String, Object> variables) {
}
