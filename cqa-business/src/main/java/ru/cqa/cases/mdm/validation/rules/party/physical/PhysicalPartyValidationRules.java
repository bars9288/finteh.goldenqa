package ru.cqa.cases.mdm.validation.rules.party.physical;

import java.util.ArrayList;
import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.cases.mdm.validation.rules.party.PartyValidationRules;
import ru.cqa.cases.mdm.validation.rules.party.common.AttributeAddressRule;
import ru.cqa.cases.mdm.validation.rules.party.common.AttributeEmailRule;
import ru.cqa.cases.mdm.validation.rules.party.common.AttributePhoneRule;
import ru.cqa.model.mdm.PartyDTO;

public final class PhysicalPartyValidationRules extends PartyValidationRules {

    public List<Rule<PartyDTO>> rules() {
        ArrayList<Rule<PartyDTO>> rules = new ArrayList<>(super.commonRules());
        rules.addAll(
                List.of(
                        new PhysicalPartyAttributeTypeRule(),
                        new FullNameRule(),
                        new BirthDateRule(),
                        new DeathDateRule(),
                        new GenderRule(),
                        new BirthPlaceRule(),
                        new CitizenshipRule(),
                        new ForeignMonetaryResidentRule(),
                        new InnRule(),
                        new SnilsRule(),
                        new VipRule(),
                        new BankEmployeeRule(),
                        new AttributeDocumentRule(),
                        new AttributeAddressRule(),
                        new AttributePhoneRule(),
                        new AttributeEmailRule()));
        return rules;
    }
}
