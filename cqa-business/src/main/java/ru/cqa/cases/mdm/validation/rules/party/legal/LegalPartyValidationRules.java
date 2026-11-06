package ru.cqa.cases.mdm.validation.rules.party.legal;

import java.util.ArrayList;
import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.cases.mdm.validation.rules.party.PartyValidationRules;
import ru.cqa.model.mdm.PartyDTO;

public final class LegalPartyValidationRules extends PartyValidationRules {

    public List<Rule<PartyDTO>> rules() {
        ArrayList<Rule<PartyDTO>> rules = new ArrayList<>(super.commonRules());
        rules.addAll(
                List.of(
                        new LegalPartyAttributeTypeRule()
                ));
        return rules;
    }
}
