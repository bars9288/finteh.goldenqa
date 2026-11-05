package ru.cqa.server.web.scenario;

import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.cqa.scenario.runtime.execution.ExecutionParameters;
import ru.cqa.scenario.runtime.execution.ScenarioEngine;
import ru.cqa.scenario.runtime.result.ExecutionResult;

import static ru.cqa.server.web.ApiPaths.SCENARIOS;

@Tag(name = "Сценарии", description = "Создание и запуск сценариев CQA")
@RestController
@RequestMapping(path = SCENARIOS, produces = MediaType.APPLICATION_JSON_VALUE)
public class ScenarioExecutionController {
    private final ScenarioEngine scenarioEngine;

    public ScenarioExecutionController(ScenarioEngine scenarioEngine) {
        this.scenarioEngine = scenarioEngine;
    }

    @Operation(summary = "Запустить сохранённый сценарий")
    @PostMapping(path = "/{scenarioId}/executions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExecutionResult> execute(
            @PathVariable UUID scenarioId,
            @Valid @RequestBody ScenarioExecutionRequest request) {
        ExecutionParameters parameters = new ExecutionParameters(
                request.version(),
                request.environmentId(),
                request.variables() == null ? Map.of() : request.variables());
        return ResponseEntity.ok(scenarioEngine.execute(scenarioId, parameters));
    }

    public record ScenarioExecutionRequest(
            @NotBlank String version,
            @NotBlank String environmentId,
            Map<String, Object> variables) {
    }
}
