package ru.cqa.cases.mdm.validation.rules.party.common;

import java.util.ArrayList;
import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.model.mdm.AttributeDTO;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.AttributeFieldNames.RAW_SOURCE;
import static ru.cqa.cases.mdm.constants.AttributeTypes.EMAIL;

/**
 * 22. Правило атрибута — почта {@code EMAIL}: адрес передаётся строкой в реквизите
 * {@code rawSource}, обязателен и содержит {@code @}.
 */
public final class AttributeEmailRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        List<AttributeDTO> attributes = party.attribute();
        if (attributes == null) {
            return List.of();
        }
        List<String> errors = new ArrayList<>();
        for (int attributeIndex = 0; attributeIndex < attributes.size(); attributeIndex++) {
            AttributeDTO attribute = attributes.get(attributeIndex);
            if (attribute == null || !EMAIL.equals(attribute.type())) {
                continue;
            }
            String fieldPath = "party[0].attribute[" + attributeIndex + "].field." + RAW_SOURCE;
            String emailValue = FieldMaps.of(attribute).get(RAW_SOURCE);
            if (isBlank(emailValue)) {
                errors.add(fieldPath + ": обязательно для " + EMAIL);
            } else if (!emailValue.contains("@")) {
                errors.add(fieldPath + ": для " + EMAIL + " ожидается адрес со знаком @");
            }
        }
        return errors;
    }

}
