package ru.cqa.api.validation;

import java.util.List;

public final class ScenarioValidatorException extends ValidationException {

    public ScenarioValidatorException(List<String> errors) {
        super(errors);
    }
}
