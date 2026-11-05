package ru.cqa.scenario.creation;

import java.util.Objects;

import ru.cqa.scenario.definition.model.Scenario;
import ru.cqa.scenario.definition.validation.ScenarioValidator;
import ru.cqa.scenario.repository.ScenarioRepository;
import ru.cqa.scenario.serialization.ScenarioParser;

/**
 * Координирует разбор JSON, валидацию и сохранение сценария.
 */
public final class ScenarioCreationService {
    private final ScenarioParser parser;
    private final ScenarioValidator validator;
    private final ScenarioRepository repository;

    public ScenarioCreationService(ScenarioParser parser, ScenarioValidator validator, ScenarioRepository repository) {
        this.parser = parser;
        this.validator = validator;
        this.repository = repository;
    }

    public Scenario create(String json) {
        Scenario scenario = parser.parse(json);
        validator.validateBody(scenario);
        if (repository.findByIdAndVersion(scenario.id(), scenario.version()).isPresent()) {
            throw new ScenarioAlreadyExistsException(scenario.id(), scenario.version());
        }
        return repository.save(scenario);
    }
}
