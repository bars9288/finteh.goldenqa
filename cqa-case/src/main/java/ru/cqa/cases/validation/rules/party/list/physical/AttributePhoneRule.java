package ru.cqa.cases.validation.rules.party.list.physical;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.AttributeDTO;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;

import static ru.cqa.cases.validation.constants.AttributesRequisitesNames.RAW_SOURCE;
import static ru.cqa.cases.validation.constants.AttributesTypes.PHONE;

public class AttributePhoneRule  implements Rule<PartyDTO> {

    //21. Правило атрибута - телефон PHONE: телефон передаётся строкой в реквизите rawSource, обязателен (не null и не пустой)

    @Override
    public List<String> check(PartyDTO party) {
        List<AttributeDTO> attrs = party.attribute();
        if (attrs == null) {
            return List.of();
        }
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < attrs.size(); i++) {
            AttributeDTO a = attrs.get(i);
            if (a == null || !PHONE.equals(a.type())) {
                continue;
            }
            if (isBlank(a.fieldMap().get(RAW_SOURCE))) {
                errors.add("party[0].attribute[" + i + "].field." + RAW_SOURCE + ": обязательно для " + PHONE);
            }
        }
        return errors;
    }


}
