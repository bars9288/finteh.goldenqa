package ru.cqa.validators;

import ru.cqa.validation.Rule;
import ru.cqa.validation.Validator;
import ru.cqa.dto.OperatorDTO;

import java.util.List;

public class GreetingValidator extends Validator<OperatorDTO, OperatorDTO, GreetingValidatorException> {

    public GreetingValidator(jakarta.validation.Validator validator, List<Rule<OperatorDTO>> rules){
        super(validator, rules);
    }

    @Override
    protected OperatorDTO entity(OperatorDTO request) {
        return request;
    }

    @Override
    protected GreetingValidatorException exception(List<String> errors) {
        return new GreetingValidatorException(errors);
    }
}
