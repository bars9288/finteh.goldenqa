package ru.cqa.cases.mdm.validation.rules.party.common;

import ru.cqa.cases.mdm.validation.rules.Rule;
import ru.cqa.utils.FieldMaps;
import ru.cqa.dto.AttributeDTO;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;

import static ru.cqa.cases.mdm.constants.AttributesRequisitesNames.ATTR_TYPE;
import static ru.cqa.cases.mdm.constants.AttributesTypes.ADDRESS;

/**
 * 20. Правило атрибута — адрес {@code ADDRESS}: реквизит {@code type} обязателен (не null и не пустой).
 *
 * <p>Вопрос по корректной логике: ИЛИ {@code rawSource}, ИЛИ набор полей из {@code ГАР / ФИАС / КЛАДР}.
 */
public final class AttributeAddressRule implements Rule<PartyDTO> {

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
            if (isBlank(FieldMaps.of(a).get(ATTR_TYPE))) {
                errors.add("party[0].attribute[" + i + "].field." + ATTR_TYPE + ": обязательно для " + ADDRESS);
            }
        }
        return errors;
    }
}
