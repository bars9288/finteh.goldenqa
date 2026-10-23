package ru.cqa.cases.validation.rules.party.list.physical;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;

import java.util.List;

import static ru.cqa.cases.validation.constants.RequisitesNames.INN;
import static ru.cqa.cases.validation.constants.RequisitesNames.INN_RAW_SOURCE;

public class InnRule  implements Rule<PartyDTO> {

    // ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица
    //14. ИНН — только в innRawSource; поле inn не заполняется

    @Override
    public List<String> check(PartyDTO party) {
        if (!isBlank(party.fieldMap().get(INN))) {
            return List.of("party[0].field." + INN + ": не заполняется, ИНН указывайте в " + INN_RAW_SOURCE);
        }
        return List.of();
    }
}
