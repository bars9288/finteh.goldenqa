package ru.cqa.cases.validation.rules.party.list.physical;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.AttributeDTO;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;

import static ru.cqa.cases.validation.constants.AttributesRequisitesNames.ATTR_TYPE;
import static ru.cqa.cases.validation.constants.AttributesTypes.ADDRESS;

public class AttributeAddressRule implements Rule<PartyDTO> {

    //20. Правило атрибута - адрес ADDRESS: реквизит type обязателен (не null и не пустой)
    // вопрос по корретной логике ИЛИ ИЛИ rawSource и набора полей из ГАР ФИАС КЛАДР

    @Override
    public List<String> check(PartyDTO party) {
        List<AttributeDTO> attrs = party.attribute();
        if (attrs == null) {
            return List.of();
        }
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < attrs.size(); i++) {
            AttributeDTO a = attrs.get(i);
            if (a == null || !ADDRESS.equals(a.type())) {
                continue;
            }
            if (isBlank(a.fieldMap().get(ATTR_TYPE))) {
                errors.add("party[0].attribute[" + i + "].field." + ATTR_TYPE + ": обязательно для " + ADDRESS);
            }
        }
        return errors;
    }
}
