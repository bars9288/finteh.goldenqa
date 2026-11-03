package ru.cqa.cases.mdm.validation.rules.party.common;

import ru.cqa.validation.Rule;
import ru.cqa.utils.FieldMaps;
import ru.cqa.dto.AttributeDTO;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;

import static ru.cqa.cases.mdm.constants.AttributesRequisitesNames.RAW_SOURCE;
import static ru.cqa.cases.mdm.constants.AttributesTypes.EMAIL;

/**
 * 22. Правило атрибута — почта {@code EMAIL}: адрес передаётся строкой в реквизите
 * {@code rawSource}, обязателен и содержит {@code @}.
 */
public final class AttributeEmailRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        List<AttributeDTO> attrs = party.attribute();
        if (attrs == null) {
            return List.of();
        }
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < attrs.size(); i++) {
            AttributeDTO a = attrs.get(i);
            if (a == null || !EMAIL.equals(a.type())) {
                continue;
            }
            String path = "party[0].attribute[" + i + "].field." + RAW_SOURCE;
            String raw = FieldMaps.of(a).get(RAW_SOURCE);
            if (isBlank(raw)) {
                errors.add(path + ": обязательно для " + EMAIL);
            } else if (!raw.contains("@")) {
                errors.add(path + ": для " + EMAIL + " ожидается адрес со знаком @");
            }
        }
        return errors;
    }

}
