package ru.cqa.cases.validation.rules.party;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.cases.validation.rules.party.list.PartyRules;
import ru.cqa.cases.validation.rules.party.list.physical.*;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;

public final class LegalPartyRules extends PartyRules {

    public List<Rule<PartyDTO>> values() {
        ArrayList<Rule<PartyDTO>> rules = new ArrayList<>(super.generalValues());
        rules.addAll(
                List.of(

                ));
        return rules;
    }
}
