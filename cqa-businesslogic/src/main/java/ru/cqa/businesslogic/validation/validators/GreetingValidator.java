package ru.cqa.businesslogic.validation.validators;

import ru.cqa.businesslogic.exceptions.GreetingValidationException;
import ru.cqa.businesslogic.exceptions.ValidationException;
import ru.cqa.businesslogic.validation.rules.Rule;
import ru.cqa.dto.OperatorDTO;

import java.util.List;

public class GreetingValidator extends Validator<OperatorDTO, OperatorDTO> {

    public GreetingValidator(jakarta.validation.Validator validator, List<Rule<OperatorDTO>> rules){
        super(validator, rules);
    }

    @Override
    protected OperatorDTO entity(OperatorDTO request) {
        return request;
    }

    @Override
    protected ValidationException exception(List<String> errors) {
        return new GreetingValidationException(errors);
    }
}
