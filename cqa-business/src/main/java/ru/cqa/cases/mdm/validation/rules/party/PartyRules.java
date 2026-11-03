package ru.cqa.cases.mdm.validation.rules.party;

import ru.cqa.cases.mdm.validation.rules.Rule;
import ru.cqa.cases.mdm.validation.rules.party.common.*;
import ru.cqa.mdm.validation.rules.party.common.*;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;

public abstract class PartyRules {

    public final ArrayList<Rule<PartyDTO>> generalValues() {
        ArrayList<Rule<PartyDTO>> rules = new ArrayList<>();
        rules.addAll(
                List.of(
                        new ActualitydateAttrRule(),
                        new ActualitydateRule(),
                        new AttributeTypeRule(),
                        new CreateddateRule(),
                        new IdentificationLevelRule(),
                        new PrimaryRule()
                ));
        return  rules;
    }

    abstract public List<Rule<PartyDTO>> ruleValues();
}
