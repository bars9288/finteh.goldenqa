package ru.cqa.cases.mdm.validation.rules.party.common;

import java.util.ArrayList;
import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.model.mdm.AttributeDTO;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.AttributeFieldNames.ATTRIBUTE_TYPE;
import static ru.cqa.cases.mdm.constants.AttributeTypes.ADDRESS;

/**
 * 20. Правило атрибута — адрес {@code ADDRESS}: реквизит {@code type} обязателен (не null и не пустой).
 *
 * <p>Вопрос по корректной логике: ИЛИ {@code rawSource}, ИЛИ набор полей из {@code ГАР / ФИАС / КЛАДР}.
 */
public final class AttributeAddressRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        List<AttributeDTO> attributes = party.attribute();
        if (attributes == null) {
            return List.of();
        }
        List<String> errors = new ArrayList<>();
        for (int attributeIndex = 0; attributeIndex < attributes.size(); attributeIndex++) {
            AttributeDTO attribute = attributes.get(attributeIndex);
            if (attribute == null || !ADDRESS.equals(attribute.type())) {
                continue;
            }
            if (isBlank(FieldMaps.of(attribute).get(ATTRIBUTE_TYPE))) {
                errors.add("party[0].attribute[" + attributeIndex + "].field." + ATTRIBUTE_TYPE + ": обязательно для " + ADDRESS);
            }
        }
        return errors;
    }
}
