package ru.cqa.cases.validation.rules.party.list.general;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;

import java.util.List;

import static ru.cqa.cases.validation.constants.RequisitesNames.IDENTIFICATION_LEVEL;

public class IdentificationLevelRule implements Rule<PartyDTO> {

//   *** ПРОВЕРКА ОБЯЗАТЕЛЬНЫХ КЛЮЧЕЙ ДЛЯ PARTY
//1. ОБЩАЯ проверка: Поле уровень идентификации обязателен
//   Применяется для всех типов Party (Физическое лицо / Юридическое лицо)

    @Override
    public List<String> check(PartyDTO party) {
        if (isBlank(party.fieldMap().get(IDENTIFICATION_LEVEL))) {
            return List.of("party[0].field." + IDENTIFICATION_LEVEL + ": обязательно, не должно быть пустым");
        }
        return List.of();
    }
}
