package ru.cqa.cases.mdm.validation.rules.party.physical;

import java.util.List;
import java.util.Map;
import ru.cqa.api.validation.Rule;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.PartyFieldNames.BIRTH_PLACE;

/**
 * 11. Место рождения — только из {@code birthPlace}, обязательно;
 * {@code birthPlaceCountry}, {@code birthPlaceRegion} и т.п. его не заменяют.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class BirthPlaceRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        Map<String, String> fieldsByName = FieldMaps.of(party);
        if (isBlank(fieldsByName.get(BIRTH_PLACE))) {
            return List.of("party[0].field." + BIRTH_PLACE + ": не должно быть пустым — место рождения читается только из " + BIRTH_PLACE);
        }
        return List.of();
    }

}
