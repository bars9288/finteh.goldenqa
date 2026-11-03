package ru.cqa.cases.mdm.validation.rules.party.common;

import ru.cqa.cases.mdm.validation.rules.Rule;
import ru.cqa.utils.FieldMaps;
import ru.cqa.dto.AttributeDTO;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;

import static ru.cqa.cases.mdm.constants.AttributesRequisitesNames.PRIMARY;

/**
 * 5. Правило атрибута — ОБЩАЯ проверка: ни у одного атрибута не допускается заполненный
 * реквизит {@code primary} (null или пусто — ок).
 *
 * <p>Применяется для всех типов Party (Физическое лицо / Юридическое лицо).
 */
public final class PrimaryRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        List<AttributeDTO> attrs = party.attribute();
        if (attrs == null) {
            return List.of();
        }
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < attrs.size(); i++) {
            AttributeDTO a = attrs.get(i);
            if (a != null && !isBlank(FieldMaps.of(a).get(PRIMARY))) {
                errors.add("party[0].attribute[" + i + "].field." + PRIMARY + ": не заполняется");
            }
        }
        return errors;
    }
}
