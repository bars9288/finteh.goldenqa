package ru.cqa.cases.validation.rules.party.list.general;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;

import java.util.List;

import static ru.cqa.cases.validation.constants.RequisitesNames.IDENTIFICATION_LEVEL;

/**
 * 1. ОБЩАЯ проверка: поле уровень идентификации ({@code identificationLevel}) обязательно.
 *
 * <p>ПРОВЕРКА ОБЯЗАТЕЛЬНЫХ КЛЮЧЕЙ ДЛЯ PARTY.
 * Применяется для всех типов Party (Физическое лицо / Юридическое лицо).
 */
public final class IdentificationLevelRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        if (isBlank(party.fieldMap().get(IDENTIFICATION_LEVEL))) {
            return List.of("party[0].field." + IDENTIFICATION_LEVEL + ": обязательно, не должно быть пустым");
        }
        return List.of();
    }
}
