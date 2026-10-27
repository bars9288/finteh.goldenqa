package ru.cqa.businesslogic.validation.validators;

import ru.cqa.businesslogic.exceptions.SaveAndMergeValidationException;
import ru.cqa.businesslogic.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;
import ru.cqa.dto.SaveAndMergeDTO;

import java.util.List;

public class SaveAndMergeValidator extends Validator<SaveAndMergeDTO, PartyDTO> {

    public SaveAndMergeValidator(jakarta.validation.Validator validator, List<Rule<PartyDTO>> rules){
        super(validator, rules);
    }

    @Override
    protected PartyDTO entity(SaveAndMergeDTO request) {
        List<PartyDTO> parties = request.party();
        return parties == null || parties.isEmpty() ? null : parties.get(0);
    }

    @Override
    protected SaveAndMergeValidationException exception(List<String> errors) {
        return new SaveAndMergeValidationException(errors);
    }
}
