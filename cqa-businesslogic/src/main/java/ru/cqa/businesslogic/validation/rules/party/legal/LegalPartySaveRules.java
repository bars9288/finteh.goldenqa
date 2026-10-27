package ru.cqa.businesslogic.validation.rules.party.legal;

import ru.cqa.businesslogic.validation.rules.Rule;
import ru.cqa.businesslogic.validation.rules.party.PartyRules;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;

public final class LegalPartySaveRules extends PartyRules {

    public List<Rule<PartyDTO>> ruleValues() {
        ArrayList<Rule<PartyDTO>> rules = new ArrayList<>(super.generalValues());
        rules.addAll(
                List.of(
                        new AttributeLegalFIlterRule()
                ));
        return rules;
    }
}
