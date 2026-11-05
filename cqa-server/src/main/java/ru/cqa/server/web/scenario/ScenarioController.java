package ru.cqa.server.web.scenario;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.cqa.scenario.creation.ScenarioCreationService;
import ru.cqa.scenario.definition.model.Scenario;

import static ru.cqa.server.web.ApiPaths.SCENARIOS;

@Tag(name = "Сценарии", description = "Создание сценариев CQA")
@RestController
@RequestMapping(path = SCENARIOS, produces = MediaType.APPLICATION_JSON_VALUE)
public class ScenarioController {
    private final ScenarioCreationService scenarioCreation;

    public ScenarioController(ScenarioCreationService scenarioCreation) {
        this.scenarioCreation = scenarioCreation;
    }

    @Operation(summary = "Проверить JSON сценария и сохранить его")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ScenarioCreatedResponse> create(@RequestBody byte[] rawJson) {
        String json = new String(rawJson, StandardCharsets.UTF_8);
        Scenario scenario = scenarioCreation.create(json);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ScenarioCreatedResponse(scenario.id(), scenario.version()));
    }

    public record ScenarioCreatedResponse(UUID id, String version) {
    }
}
