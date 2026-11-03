package ru.cqa.validators.mdm;

import ru.cqa.validation.Rule;
import ru.cqa.dto.PartyDTO;
import ru.cqa.dto.SaveAndMergeDTO;
import ru.cqa.validation.Validator;

import java.util.List;

public class SaveAndMergeValidator extends Validator<SaveAndMergeDTO, PartyDTO, SaveAndMergeValidatorException> {

    public SaveAndMergeValidator(jakarta.validation.Validator validator, List<Rule<PartyDTO>> rules){
        super(validator, rules);
    }

    @Override
    protected PartyDTO entity(SaveAndMergeDTO request) {
        List<PartyDTO> parties = request.party();
        return parties == null || parties.isEmpty() ? null : parties.get(0);
    }

    @Override
    protected SaveAndMergeValidatorException exception(List<String> errors) {
        return new SaveAndMergeValidatorException(errors);
    }
}
