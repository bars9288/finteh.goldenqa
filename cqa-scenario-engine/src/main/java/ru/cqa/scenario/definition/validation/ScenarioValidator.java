package ru.cqa.scenario.definition.validation;

import ru.cqa.scenario.definition.model.Scenario;
import ru.cqa.validation.Validator;

import java.util.List;

public class ScenarioValidator extends Validator<Scenario, Scenario, ScenarioValidatorException> {

    public ScenarioValidator(jakarta.validation.Validator validator){
        super(validator, List.of());
    }

    @Override
    protected Scenario entity(Scenario request) {
        return request;
    }

    @Override
    protected ScenarioValidatorException exception(List<String> errors) {
        return new ScenarioValidatorException(errors);
    }
}
