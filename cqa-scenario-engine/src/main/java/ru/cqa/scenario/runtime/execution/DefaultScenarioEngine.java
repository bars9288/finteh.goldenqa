package ru.cqa.scenario.runtime.execution;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import ru.cqa.scenario.definition.model.Scenario;
import ru.cqa.scenario.definition.validation.ScenarioValidator;
import ru.cqa.scenario.repository.ScenarioRepository;
import ru.cqa.scenario.runtime.result.ExecutionResult;

/** Загружает и проверяет сохранённый сценарий, затем передаёт его стратегии исполнения. */
public final class DefaultScenarioEngine implements ScenarioEngine {
    private final ScenarioRepository repository;
    private final ScenarioValidator validator;
    private final ExecutionStrategy strategy;

    public DefaultScenarioEngine(
            ScenarioRepository repository,
            ScenarioValidator validator,
            ExecutionStrategy strategy) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.validator = Objects.requireNonNull(validator, "validator");
        this.strategy = Objects.requireNonNull(strategy, "strategy");
    }

    @Override
    public ExecutionResult execute(UUID scenarioId, ExecutionParameters parameters) {
        Objects.requireNonNull(scenarioId, "scenarioId");
        Objects.requireNonNull(parameters, "parameters");
        String version = Objects.requireNonNull(parameters.version(), "version");

        Scenario scenario = repository.findByIdAndVersion(scenarioId, version)
                .orElseThrow(() -> new ScenarioNotFoundException(scenarioId, version));
        validator.validateBody(scenario);

        ExecutionContext context = new ExecutionContext(
                UUID.randomUUID(),
                scenario.id(),
                parameters.environmentId(),
                parameters.variables() == null ? Map.of() : parameters.variables());
        return strategy.execute(scenario, context);
    }
}
