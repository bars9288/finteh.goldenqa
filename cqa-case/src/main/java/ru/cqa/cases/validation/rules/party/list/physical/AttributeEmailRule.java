package ru.cqa.cases.validation.rules.party.list.physical;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.AttributeDTO;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;

import static ru.cqa.cases.validation.constants.AttributesRequisitesNames.RAW_SOURCE;
import static ru.cqa.cases.validation.constants.AttributesTypes.EMAIL;

public class AttributeEmailRule implements Rule<PartyDTO> {

//22. Правило атрибута - почта EMAIL: адрес передаётся строкой в реквизите rawSource, обязателен и содержит @

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
            String raw = a.fieldMap().get(RAW_SOURCE);
            if (isBlank(raw)) {
                errors.add(path + ": обязательно для " + EMAIL);
            } else if (!raw.contains("@")) {
                errors.add(path + ": для " + EMAIL + " ожидается адрес со знаком @");
            }
        }
        return errors;
    }

}
