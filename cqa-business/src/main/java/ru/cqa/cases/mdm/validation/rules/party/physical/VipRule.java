package ru.cqa.cases.mdm.validation.rules.party.physical;

import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.cases.mdm.constants.BooleanFieldValues;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.PartyFieldNames.VIP;

/**
 * 16. VIP — {@code TRUE}, {@code FALSE} или {@code UNKNOWN}; пустое допустимо.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class VipRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        String vipValue = FieldMaps.of(party).get(VIP);
        if (!isBlank(vipValue) && !BooleanFieldValues.ALLOWED_VALUES.contains(vipValue.strip())) {
            return List.of("party[0].field." + VIP + ": допустимо TRUE, FALSE или UNKNOWN");
        }
        return List.of();
    }

}
