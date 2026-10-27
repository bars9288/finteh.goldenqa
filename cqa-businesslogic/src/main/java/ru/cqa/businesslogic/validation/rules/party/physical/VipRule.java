package ru.cqa.businesslogic.validation.rules.party.physical;

import ru.cqa.businesslogic.constants.RequisBoolFields;
import ru.cqa.businesslogic.validation.rules.Rule;
import ru.cqa.businesslogic.utils.FieldMaps;
import ru.cqa.dto.PartyDTO;

import java.util.List;

import static ru.cqa.businesslogic.constants.RequisitesNames.VIP;

/**
 * 16. VIP — {@code TRUE}, {@code FALSE} или {@code UNKNOWN}; пустое допустимо.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class VipRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        String raw = FieldMaps.of(party).get(VIP);
        if (!isBlank(raw) && !RequisBoolFields.BoolValues.contains(raw.strip())) {
            return List.of("party[0].field." + VIP + ": допустимо TRUE, FALSE или UNKNOWN");
        }
        return List.of();
    }

}
