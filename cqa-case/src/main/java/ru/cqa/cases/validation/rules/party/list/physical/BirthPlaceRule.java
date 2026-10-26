package ru.cqa.cases.validation.rules.party.list.physical;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;

import java.util.List;
import java.util.Map;

import static ru.cqa.cases.validation.constants.RequisitesNames.BIRTH_PLACE;

/**
 * 11. Место рождения — только из {@code birthPlace}, обязательно;
 * {@code birthPlaceCountry}, {@code birthPlaceRegion} и т.п. его не заменяют.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class BirthPlaceRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        Map<String, String> f = party.fieldMap();
        if (isBlank(f.get(BIRTH_PLACE))) {
            return List.of("party[0].field." + BIRTH_PLACE + ": не должно быть пустым — место рождения читается только из " + BIRTH_PLACE);
        }
        return List.of();
    }

}
