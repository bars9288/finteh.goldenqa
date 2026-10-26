package ru.cqa.cases.validation.rules.party;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.cases.validation.rules.party.list.PartyRules;
import ru.cqa.cases.validation.rules.party.list.legal.*;
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
