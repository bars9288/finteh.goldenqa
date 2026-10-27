package ru.cqa.businesslogic.validation.rules.party.physical;

import ru.cqa.businesslogic.validation.rules.Rule;
import ru.cqa.businesslogic.utils.FieldMaps;
import ru.cqa.dto.AttributeDTO;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.cqa.businesslogic.constants.AttributesRequisitesNames.*;
import static ru.cqa.businesslogic.constants.AttributesTypes.DOCUMENT_PASSPORT;
import static ru.cqa.businesslogic.constants.AttributesTypes.PASSPORT;

/**
 * 19. Правило атрибута — документ {@code DOCUMENT_PASSPORT}: обязательно наличие реквизитов
 * {@code type} = {@code PASSPORT}, поля уточнения типа документа {@code qualifierType},
 * а также обязательных реквизитов. Реквизит {@code rawSource}, если передан, должен быть пустым.
 */
public final class AttributeDocumentRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        List<AttributeDTO> attrs = party.attribute();
        if (attrs == null) {
            return List.of();
        }
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < attrs.size(); i++) {
            AttributeDTO a = attrs.get(i);
            if (a == null || !DOCUMENT_PASSPORT.equals(a.type())) {
                continue;
            }
            Map<String, String> f = FieldMaps.of(a);
            String path = "party[0].attribute[" + i + "].field.";
            String type = f.get(ATTR_TYPE);
            if (isBlank(type) || !PASSPORT.equals(type.strip())) {
                errors.add(path + ATTR_TYPE + ": для " + DOCUMENT_PASSPORT + " ожидается только " + PASSPORT);
            }
            if (isBlank(f.get(QUALIFIER_TYPE))) {
                errors.add(path + QUALIFIER_TYPE + ": обязательно для " + DOCUMENT_PASSPORT);
            }
            if (!isBlank(f.get(RAW_SOURCE))) {
                errors.add(path + RAW_SOURCE + ": должно быть пустым");
            }
        }
        return errors;
    }
}
