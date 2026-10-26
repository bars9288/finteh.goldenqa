package ru.cqa.cases.validation.rules.party.list;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.cases.validation.rules.party.list.general.*;
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
