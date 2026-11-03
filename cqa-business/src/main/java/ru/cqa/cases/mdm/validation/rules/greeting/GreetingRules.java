package ru.cqa.cases.mdm.validation.rules.greeting;

import ru.cqa.cases.mdm.validation.rules.Rule;
import ru.cqa.dto.OperatorDTO;

import java.util.ArrayList;
import java.util.List;

public final class GreetingRules {

    public List<Rule<OperatorDTO>> ruleValues() {
        ArrayList<Rule<OperatorDTO>> rules = new ArrayList<>();
        rules.addAll(
                List.of(
                        new NameFamilyRule()
                ));
        return rules;
    }
}
