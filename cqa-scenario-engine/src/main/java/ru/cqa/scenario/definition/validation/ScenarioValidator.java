package ru.cqa.scenario.definition.validation;

import java.util.List;
import java.util.Objects;
import ru.cqa.api.validation.FormValidator;
import ru.cqa.api.validation.ScenarioValidatorException;
import ru.cqa.api.validation.Validator;
import ru.cqa.model.scenario.Scenario;

public final class ScenarioValidator implements Validator<Scenario> {
    private final FormValidator formValidator;

    public ScenarioValidator(FormValidator formValidator) {
        this.formValidator = Objects.requireNonNull(formValidator, "formValidator");
    }

    @Override
    public void validateBody(Scenario request) {
        List<String> errors = formValidator.validate(request);
        if (!errors.isEmpty()) {
            throw new ScenarioValidatorException(errors);
        }
    }
}
