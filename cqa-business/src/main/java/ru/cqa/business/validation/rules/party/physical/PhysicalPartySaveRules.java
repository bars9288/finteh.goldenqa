package ru.cqa.business.validation.rules.party.physical;

import ru.cqa.business.validation.rules.Rule;
import ru.cqa.business.validation.rules.party.PartyRules;
import ru.cqa.business.validation.rules.party.common.AttributeAddressRule;
import ru.cqa.business.validation.rules.party.common.AttributeEmailRule;
import ru.cqa.business.validation.rules.party.common.AttributePhoneRule;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;


public final class PhysicalPartySaveRules extends PartyRules {

    public List<Rule<PartyDTO>> ruleValues() {
        ArrayList<Rule<PartyDTO>> rules = new ArrayList<>(super.generalValues());
        rules.addAll(
                List.of(
                        new AttributePhysicFIlterRule(),
                        new FIOchekRule(),
                        new BirthdayDateRule(),
                        new DeathDateRule(),
                        new GenderRule(),
                        new BirthPlaceRule(),
                        new CitizenshipRule(),
                        new ForMonetResidRule(),
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
