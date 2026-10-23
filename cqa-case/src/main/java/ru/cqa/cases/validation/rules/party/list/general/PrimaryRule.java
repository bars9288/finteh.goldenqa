package ru.cqa.cases.validation.rules.party.list.general;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.AttributeDTO;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;

import static ru.cqa.cases.validation.constants.AttributesRequisitesNames.PRIMARY;

public class PrimaryRule implements Rule<PartyDTO> {

    //5. Правило атрибута - ОБЩАЯ проверка: ни у одного атрибута не допускается заполненный реквизит primary
    // (null или пусто — ок)
    // Применяется для всех типов Party (Физическое лицо / Юридическое лицо)

    @Override
    public List<String> check(PartyDTO party) {
        List<AttributeDTO> attrs = party.attribute();
        if (attrs == null) {
            return List.of();
        }
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < attrs.size(); i++) {
            AttributeDTO a = attrs.get(i);
            if (a != null && !isBlank(a.fieldMap().get(PRIMARY))) {
                errors.add("party[0].attribute[" + i + "].field." + PRIMARY + ": не заполняется");
            }
        }
        return errors;
    }
}
