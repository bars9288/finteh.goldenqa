package ru.cqa.cases.mdm.validation.rules.party.physical;

import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.PartyFieldNames.INN;
import static ru.cqa.cases.mdm.constants.PartyFieldNames.INN_RAW_SOURCE;

/**
 * 14. ИНН — только в {@code innRawSource}; поле {@code inn} не заполняется.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class InnRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        if (!isBlank(FieldMaps.of(party).get(INN))) {
            return List.of("party[0].field." + INN + ": не заполняется, ИНН указывайте в " + INN_RAW_SOURCE);
        }
        return List.of();
    }
}
