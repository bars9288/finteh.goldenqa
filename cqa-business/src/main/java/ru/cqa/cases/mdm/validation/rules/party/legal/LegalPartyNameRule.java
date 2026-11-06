package ru.cqa.cases.mdm.validation.rules.party.legal;

import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.model.mdm.PartyDTO;

public final class LegalPartyNameRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        return List.of();
    }
}
