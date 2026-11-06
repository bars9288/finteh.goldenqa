package ru.cqa.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ru.cqa.api.validation.FormValidator;
import ru.cqa.api.validation.GreetingValidatorException;
import ru.cqa.api.validation.Rule;
import ru.cqa.api.validation.Validator;
import ru.cqa.model.greeting.OperatorDTO;

/** Проверяет форму и предметные правила, собирая все нарушения в один список. */
public final class GreetingValidator implements Validator<OperatorDTO> {
    private final FormValidator formValidator;
    private final List<Rule<OperatorDTO>> rules;

    public GreetingValidator(FormValidator formValidator, List<Rule<OperatorDTO>> rules) {
        this.formValidator = Objects.requireNonNull(formValidator, "formValidator");
        this.rules = List.copyOf(rules);
    }

    @Override
    public void validateBody(OperatorDTO request) {
        List<String> errors = new ArrayList<>(formValidator.validate(request));
        OperatorDTO entity = request;
        if (entity != null) {
            rules.forEach(rule -> errors.addAll(rule.check(entity)));
        }
        if (!errors.isEmpty()) {
            throw new GreetingValidatorException(errors);
        }
    }
}
