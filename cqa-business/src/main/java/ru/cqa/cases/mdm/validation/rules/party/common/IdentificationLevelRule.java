package ru.cqa.cases.mdm.validation.rules.party.common;

import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.PartyFieldNames.IDENTIFICATION_LEVEL;

/**
 * 1. ОБЩАЯ проверка: поле уровень идентификации ({@code identificationLevel}) обязательно.
 *
 * <p>ПРОВЕРКА ОБЯЗАТЕЛЬНЫХ КЛЮЧЕЙ ДЛЯ PARTY.
 * Применяется для всех типов Party (Физическое лицо / Юридическое лицо).
 */
public final class IdentificationLevelRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        if (isBlank(FieldMaps.of(party).get(IDENTIFICATION_LEVEL))) {
            return List.of("party[0].field." + IDENTIFICATION_LEVEL + ": обязательно, не должно быть пустым");
        }
        return List.of();
    }
}
