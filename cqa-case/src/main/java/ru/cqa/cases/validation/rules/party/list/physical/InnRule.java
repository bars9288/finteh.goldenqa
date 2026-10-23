package ru.cqa.cases.validation.rules.party.list.physical;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;

import java.util.List;

import static ru.cqa.cases.validation.constants.RequisitesNames.INN;
import static ru.cqa.cases.validation.constants.RequisitesNames.INN_RAW_SOURCE;

/**
 * 14. ИНН — только в {@code innRawSource}; поле {@code inn} не заполняется.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public class InnRule  implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        if (!isBlank(party.fieldMap().get(INN))) {
            return List.of("party[0].field." + INN + ": не заполняется, ИНН указывайте в " + INN_RAW_SOURCE);
        }
        return List.of();
    }
}
