package ru.cqa.cases.mdm.validation.rules.party.physical;

import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.cases.mdm.constants.BooleanFieldValues;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.PartyFieldNames.FOREIGN_MONETARY_RESIDENT_FLAG;

/**
 * 13. Признак валютного резидентства — {@code TRUE}, {@code FALSE} или {@code UNKNOWN},
 * регистр не важен; пустое допустимо.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class ForeignMonetaryResidentRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
            String residentFlagValue = FieldMaps.of(party).get(FOREIGN_MONETARY_RESIDENT_FLAG);
            if (!isBlank(residentFlagValue) && !BooleanFieldValues.ALLOWED_VALUES.contains(residentFlagValue.strip())) {
                return List.of("party[0].field." + FOREIGN_MONETARY_RESIDENT_FLAG + ": допустимо TRUE, FALSE или UNKNOWN");
            }
            return List.of();
    }
}
