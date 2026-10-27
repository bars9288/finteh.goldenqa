package ru.cqa.businesslogic.validation.rules.party.common;

import ru.cqa.businesslogic.validation.rules.Rule;
import ru.cqa.businesslogic.utils.FieldMaps;
import ru.cqa.dto.AttributeDTO;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;

import static ru.cqa.businesslogic.constants.AttributesRequisitesNames.ATTR_TYPE;

/**
 * 4. Правило атрибута — ОБЩАЯ проверка: у каждого атрибута обязателен реквизит {@code type}
 * (не null и не пустой).
 *
 * <p>ПРОВЕРКА ОБЯЗАТЕЛЬНЫХ КЛЮЧЕЙ ДЛЯ PARTY → ATTRIBUTE → FIELD.
 * Применяется для всех типов Party (Физическое лицо / Юридическое лицо).
 */
public final class AttributeTypeRule  implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        List<AttributeDTO> attrs = party.attribute();
        if (attrs == null) {
            return List.of();
        }
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < attrs.size(); i++) {
            AttributeDTO a = attrs.get(i);
            if (a != null && isBlank(FieldMaps.of(a).get(ATTR_TYPE))) {
                errors.add("party[0].attribute[" + i + "].field." + ATTR_TYPE + ": обязательно, не должно быть пустым");
            }
        }
        return errors;
    }
}
