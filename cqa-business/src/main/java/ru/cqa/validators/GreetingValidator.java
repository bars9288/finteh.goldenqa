package ru.cqa.validators;

import ru.cqa.cases.mdm.exceptions.GreetingValidationException;
import ru.cqa.cases.mdm.exceptions.ValidationException;
import ru.cqa.cases.mdm.validation.rules.Rule;
import ru.cqa.dto.OperatorDTO;

import java.util.List;

public class GreetingValidator extends Validator<OperatorDTO, OperatorDTO, GreetingValidationException> {

    public GreetingValidator(jakarta.validation.Validator validator, List<Rule<OperatorDTO>> rules){
        super(validator, rules);
    }

    @Override
    protected OperatorDTO entity(OperatorDTO request) {
        return request;
    }

    @Override
    protected GreetingValidationException exception(List<String> errors) {
        return new GreetingValidationException(errors);
    }
}
