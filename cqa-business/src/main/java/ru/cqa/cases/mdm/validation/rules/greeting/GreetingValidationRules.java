package ru.cqa.cases.mdm.validation.rules.greeting;

import java.util.ArrayList;
import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.model.greeting.OperatorDTO;

public final class GreetingValidationRules {

    public List<Rule<OperatorDTO>> rules() {
        ArrayList<Rule<OperatorDTO>> rules = new ArrayList<>();
        rules.addAll(
                List.of(
                        new OperatorNameRule()
                ));
        return rules;
    }
}
