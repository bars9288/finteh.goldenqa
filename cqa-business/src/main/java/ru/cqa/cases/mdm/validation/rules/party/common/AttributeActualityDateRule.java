package ru.cqa.cases.mdm.validation.rules.party.common;

import java.util.ArrayList;
import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.model.mdm.AttributeDTO;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.PartyFieldNames.ACTUALITY_DATE;
import static ru.cqa.utils.DateValidationUtils.validateDateTimeWithMoscowZone;

/**
 * 6. Правило для атрибута(-ов) — ОБЩАЯ проверка: у каждого атрибута обязателен реквизит
 * {@code actualityDate}, формат {@code ДД.ММ.ГГГГ ЧЧ:мм:СС МСК}.
 * Пояс необязателен, но если указан — только московский.
 *
 * <p>Применяется для всех типов Party (Физическое лицо / Юридическое лицо).
 */
public final class AttributeActualityDateRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        List<AttributeDTO> attributes = party.attribute();
        if (attributes == null) {
            return List.of();
        }
        List<String> errors = new ArrayList<>();
        for (int attributeIndex = 0; attributeIndex < attributes.size(); attributeIndex++) {
            AttributeDTO attribute = attributes.get(attributeIndex);
            if (attribute == null) {
                continue;
            }
            String fieldPath = "party[0].attribute[" + attributeIndex + "].field. ";
            String actualityDateValue = FieldMaps.of(attribute).get(ACTUALITY_DATE);
            if (isBlank(actualityDateValue)) {
                errors.add(fieldPath + ACTUALITY_DATE + ": обязательно, не должно быть пустым");
            } else {
                errors.addAll(validateDateTimeWithMoscowZone(actualityDateValue.strip().split("\\s"),
                        fieldPath + ACTUALITY_DATE,
                        "ожидается дата и время в формате ДД.ММ.ГГГГ ЧЧ:мм:СС, после времени — необязательный пояс"));
            }
        }
        return errors;
    }
}
