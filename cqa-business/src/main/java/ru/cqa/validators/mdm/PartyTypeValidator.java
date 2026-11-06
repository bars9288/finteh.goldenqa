package ru.cqa.validators.mdm;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import ru.cqa.api.validation.SaveAndMergeValidatorException;
import ru.cqa.api.validation.Validator;
import ru.cqa.cases.mdm.constants.PartyType;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.model.mdm.SaveAndMergeDTO;

/** Выбирает предметный набор правил; сервер не знает о типах участников и наборах правил. */
public final class PartyTypeValidator implements Validator<SaveAndMergeDTO> {
    private final Map<PartyType, Validator<SaveAndMergeDTO>> validatorsByPartyType;

    public PartyTypeValidator(Map<PartyType, Validator<SaveAndMergeDTO>> validatorsByPartyType) {
        this.validatorsByPartyType = Map.copyOf(validatorsByPartyType);
    }

    @Override
    public void validateBody(SaveAndMergeDTO request) {
        List<PartyDTO> parties = request.party();
        String partyType = parties == null || parties.isEmpty() || parties.get(0) == null ? null : parties.get(0).type();
        Validator<SaveAndMergeDTO> validator = PartyType.of(partyType)
                .map(validatorsByPartyType::get)
                .orElseThrow(() -> new SaveAndMergeValidatorException(
                        List.of("party[0].type: ожидается одно из " + Arrays.toString(PartyType.values()))));
        validator.validateBody(request);
    }
}
