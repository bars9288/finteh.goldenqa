package ru.cqa.cases.mdm.validation.rules.party.common;

import java.util.ArrayList;
import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.model.mdm.AttributeDTO;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.AttributeFieldNames.ATTRIBUTE_TYPE;

/**
 * 4. Правило атрибута — ОБЩАЯ проверка: у каждого атрибута обязателен реквизит {@code type}
 * (не null и не пустой).
 *
 * <p>ПРОВЕРКА ОБЯЗАТЕЛЬНЫХ КЛЮЧЕЙ ДЛЯ PARTY → ATTRIBUTE → FIELD.
 * Применяется для всех типов Party (Физическое лицо / Юридическое лицо).
 */
public final class AttributeTypeRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        List<AttributeDTO> attributes = party.attribute();
        if (attributes == null) {
            return List.of();
        }
        List<String> errors = new ArrayList<>();
        for (int attributeIndex = 0; attributeIndex < attributes.size(); attributeIndex++) {
            AttributeDTO attribute = attributes.get(attributeIndex);
            if (attribute != null && isBlank(FieldMaps.of(attribute).get(ATTRIBUTE_TYPE))) {
                errors.add("party[0].attribute[" + attributeIndex + "].field." + ATTRIBUTE_TYPE + ": обязательно, не должно быть пустым");
            }
        }
        return errors;
    }
}
