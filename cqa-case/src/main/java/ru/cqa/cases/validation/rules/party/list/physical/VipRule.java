package ru.cqa.cases.validation.rules.party.list.physical;

import ru.cqa.cases.validation.constants.RequisBoolFields;
import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;

import java.util.List;

import static ru.cqa.cases.validation.constants.RequisitesNames.VIP;

public class VipRule implements Rule<PartyDTO> {

    // ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица
    //16. VIP — TRUE, FALSE или UNKNOWN; пустое допустимо

    @Override
    public List<String> check(PartyDTO party) {
        String raw = party.fieldMap().get(VIP);
        if (!isBlank(raw) && !RequisBoolFields.BoolValues.contains(raw.strip())) {
            return List.of("party[0].field." + VIP + ": допустимо TRUE, FALSE или UNKNOWN");
        }
        return List.of();
    }

}
