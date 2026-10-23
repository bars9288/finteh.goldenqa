package ru.cqa.cases.validation.rules.party.list.physical;

import ru.cqa.cases.validation.constants.RequisBoolFields;
import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;

import java.util.List;
import java.util.Locale;

import static ru.cqa.cases.validation.constants.RequisitesNames.FOREIGN_MONETARY_RESIDENT_FLAG;

public class ForMonetResidRule implements Rule<PartyDTO> {

    // ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица
    //13. Признак валютного резидентства — TRUE, FALSE или UNKNOWN, регистр не важен; пустое допустимо

    @Override
    public List<String> check(PartyDTO party) {
            String raw = party.fieldMap().get(FOREIGN_MONETARY_RESIDENT_FLAG);
            if (!isBlank(raw) && !RequisBoolFields.BoolValues.contains(raw.strip())) {
                return List.of("party[0].field." + FOREIGN_MONETARY_RESIDENT_FLAG + ": допустимо TRUE, FALSE или UNKNOWN");
            }
            return List.of();
    }
}
