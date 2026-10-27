package ru.cqa.businesslogic.validation.rules.party.common;

import ru.cqa.businesslogic.validation.rules.Rule;
import ru.cqa.businesslogic.utils.FieldMaps;
import ru.cqa.dto.AttributeDTO;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;

import static ru.cqa.businesslogic.constants.AttributesRequisitesNames.RAW_SOURCE;
import static ru.cqa.businesslogic.constants.AttributesTypes.PHONE;

/**
 * 21. Правило атрибута — телефон {@code PHONE}: телефон передаётся строкой в реквизите
 * {@code rawSource}, обязателен (не null и не пустой).
 */
public final class AttributePhoneRule  implements Rule<PartyDTO> {

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
            if (isBlank(FieldMaps.of(a).get(RAW_SOURCE))) {
                errors.add("party[0].attribute[" + i + "].field." + RAW_SOURCE + ": обязательно для " + PHONE);
            }
        }
        return errors;
    }


}
