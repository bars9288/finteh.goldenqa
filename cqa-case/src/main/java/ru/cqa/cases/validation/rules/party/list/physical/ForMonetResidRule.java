package ru.cqa.cases.validation.rules.party.list.physical;

import ru.cqa.cases.validation.constants.RequisBoolFields;
import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;

import java.util.List;

import static ru.cqa.cases.validation.constants.RequisitesNames.FOREIGN_MONETARY_RESIDENT_FLAG;

/**
 * 13. Признак валютного резидентства — {@code TRUE}, {@code FALSE} или {@code UNKNOWN},
 * регистр не важен; пустое допустимо.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class ForMonetResidRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
            String raw = party.fieldMap().get(FOREIGN_MONETARY_RESIDENT_FLAG);
            if (!isBlank(raw) && !RequisBoolFields.BoolValues.contains(raw.strip())) {
                return List.of("party[0].field." + FOREIGN_MONETARY_RESIDENT_FLAG + ": допустимо TRUE, FALSE или UNKNOWN");
            }
            return List.of();
    }
}
