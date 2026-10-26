package ru.cqa.cases.validation.rules.party;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.cases.validation.rules.party.list.PartyRules;
import ru.cqa.cases.validation.rules.party.list.general.AttributeAddressRule;
import ru.cqa.cases.validation.rules.party.list.general.AttributeEmailRule;
import ru.cqa.cases.validation.rules.party.list.general.AttributePhoneRule;
import ru.cqa.cases.validation.rules.party.list.physical.*;
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
