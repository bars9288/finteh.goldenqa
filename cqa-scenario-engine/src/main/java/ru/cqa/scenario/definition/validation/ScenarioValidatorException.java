package ru.cqa.scenario.definition.validation;

import ru.cqa.validation.ValidationException;

import java.util.List;

public final class ScenarioValidatorException extends ValidationException {

    public ScenarioValidatorException(List<String> errors) {
        super(errors);
    }
}
