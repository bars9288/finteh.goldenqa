package ru.cqa.cases.mdm.validation.rules.party.common;

import java.util.ArrayList;
import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.model.mdm.AttributeDTO;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.AttributeFieldNames.RAW_SOURCE;
import static ru.cqa.cases.mdm.constants.AttributeTypes.PHONE;

/**
 * 21. Правило атрибута — телефон {@code PHONE}: телефон передаётся строкой в реквизите
 * {@code rawSource}, обязателен (не null и не пустой).
 */
public final class AttributePhoneRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        List<AttributeDTO> attributes = party.attribute();
        if (attributes == null) {
            return List.of();
        }
        List<String> errors = new ArrayList<>();
        for (int attributeIndex = 0; attributeIndex < attributes.size(); attributeIndex++) {
            AttributeDTO attribute = attributes.get(attributeIndex);
            if (attribute == null || !PHONE.equals(attribute.type())) {
                continue;
            }
            if (isBlank(FieldMaps.of(attribute).get(RAW_SOURCE))) {
                errors.add("party[0].attribute[" + attributeIndex + "].field." + RAW_SOURCE + ": обязательно для " + PHONE);
            }
        }
        return errors;
    }

}
