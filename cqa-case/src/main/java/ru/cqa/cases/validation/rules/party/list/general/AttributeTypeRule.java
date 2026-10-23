package ru.cqa.cases.validation.rules.party.list.general;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.AttributeDTO;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;

import static ru.cqa.cases.validation.constants.AttributesRequisitesNames.ATTR_TYPE;

public class AttributeTypeRule  implements Rule<PartyDTO> {

    //   *** ПРОВЕРКА ОБЯЗАТЕЛЬНЫХ КЛЮЧЕЙ ДЛЯ PARTY -> ATTRUBUTE -> FIELD
    // 4. Правило атрибута - ОБЩАЯ проверка: у каждого атрибута обязателен реквизит type (не null и не пустой)
    //   Применяется для всех типов Party (Физическое лицо / Юридическое лицо)

    @Override
    public List<String> check(PartyDTO party) {
        List<AttributeDTO> attrs = party.attribute();
        if (attrs == null) {
            return List.of();
        }
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < attrs.size(); i++) {
            AttributeDTO a = attrs.get(i);
            if (a != null && isBlank(a.fieldMap().get(ATTR_TYPE))) {
                errors.add("party[0].attribute[" + i + "].field." + ATTR_TYPE + ": обязательно, не должно быть пустым");
            }
        }
        return errors;
    }
}
