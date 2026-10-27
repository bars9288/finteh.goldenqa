package ru.cqa.business.validation.rules.party.physical;

import ru.cqa.business.constants.RequisBoolFields;
import ru.cqa.business.validation.rules.Rule;
import ru.cqa.business.utils.FieldMaps;
import ru.cqa.dto.PartyDTO;

import java.util.List;

import static ru.cqa.business.constants.RequisitesNames.FOREIGN_MONETARY_RESIDENT_FLAG;

/**
 * 13. Признак валютного резидентства — {@code TRUE}, {@code FALSE} или {@code UNKNOWN},
 * регистр не важен; пустое допустимо.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class ForMonetResidRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
            String raw = FieldMaps.of(party).get(FOREIGN_MONETARY_RESIDENT_FLAG);
            if (!isBlank(raw) && !RequisBoolFields.BoolValues.contains(raw.strip())) {
                return List.of("party[0].field." + FOREIGN_MONETARY_RESIDENT_FLAG + ": допустимо TRUE, FALSE или UNKNOWN");
            }
            return List.of();
    }
}
