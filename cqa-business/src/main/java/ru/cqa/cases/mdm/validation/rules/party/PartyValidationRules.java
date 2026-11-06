package ru.cqa.cases.mdm.validation.rules.party;

import java.util.ArrayList;
import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.cases.mdm.validation.rules.party.common.*;
import ru.cqa.model.mdm.PartyDTO;

public abstract class PartyValidationRules {

    public final ArrayList<Rule<PartyDTO>> commonRules() {
        ArrayList<Rule<PartyDTO>> rules = new ArrayList<>();
        rules.addAll(
                List.of(
                        new AttributeActualityDateRule(),
                        new PartyActualityDateRule(),
                        new AttributeTypeRule(),
                        new PartyCreatedDateRule(),
                        new IdentificationLevelRule(),
                        new PrimaryRule()
                ));
        return rules;
    }

    public abstract List<Rule<PartyDTO>> rules();
}
