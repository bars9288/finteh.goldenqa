package ru.cqa.scenario.creation;

import ru.cqa.api.scenario.ScenarioAlreadyExistsException;
import ru.cqa.api.scenario.ScenarioCreator;
import ru.cqa.api.scenario.ScenarioParser;
import ru.cqa.api.scenario.ScenarioRepository;
import ru.cqa.api.validation.Validator;
import ru.cqa.model.scenario.Scenario;

/**
 * Координирует разбор JSON, валидацию и сохранение сценария.
 */
public final class ScenarioCreationService implements ScenarioCreator {
    private final ScenarioParser parser;
    private final Validator<Scenario> validator;
    private final ScenarioRepository repository;

    public ScenarioCreationService(ScenarioParser parser, Validator<Scenario> validator, ScenarioRepository repository) {
        this.parser = parser;
        this.validator = validator;
        this.repository = repository;
    }

    @Override
    public Scenario create(String json) {
        Scenario scenario = parser.parse(json);
        validator.validateBody(scenario);
        if (repository.findByIdAndVersion(scenario.id(), scenario.version()).isPresent()) {
            throw new ScenarioAlreadyExistsException(scenario.id(), scenario.version());
        }
        return repository.save(scenario);
    }
}
