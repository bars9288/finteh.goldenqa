package ru.cqa.cases.mdm.validation.rules.party.physical;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ru.cqa.api.validation.Rule;
import ru.cqa.model.mdm.AttributeDTO;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.AttributeFieldNames.*;
import static ru.cqa.cases.mdm.constants.AttributeTypes.DOCUMENT_PASSPORT;
import static ru.cqa.cases.mdm.constants.AttributeTypes.PASSPORT;

/**
 * 19. Правило атрибута — документ {@code DOCUMENT_PASSPORT}: обязательно наличие реквизитов
 * {@code documentType} = {@code PASSPORT}, поля уточнения типа документа {@code qualifierType},
 * а также обязательных реквизитов. Реквизит {@code rawSource}, если передан, должен быть пустым.
 */
public final class AttributeDocumentRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        List<AttributeDTO> attributes = party.attribute();
        if (attributes == null) {
            return List.of();
        }
        List<String> errors = new ArrayList<>();
        for (int attributeIndex = 0; attributeIndex < attributes.size(); attributeIndex++) {
            AttributeDTO attribute = attributes.get(attributeIndex);
            if (attribute == null || !DOCUMENT_PASSPORT.equals(attribute.type())) {
                continue;
            }
            Map<String, String> fieldsByName = FieldMaps.of(attribute);
            String fieldPath = "party[0].attribute[" + attributeIndex + "].field.";
            String documentType = fieldsByName.get(ATTRIBUTE_TYPE);
            if (isBlank(documentType) || !PASSPORT.equals(documentType.strip())) {
                errors.add(fieldPath + ATTRIBUTE_TYPE + ": для " + DOCUMENT_PASSPORT + " ожидается только " + PASSPORT);
            }
            if (isBlank(fieldsByName.get(QUALIFIER_TYPE))) {
                errors.add(fieldPath + QUALIFIER_TYPE + ": обязательно для " + DOCUMENT_PASSPORT);
            }
            if (!isBlank(fieldsByName.get(RAW_SOURCE))) {
                errors.add(fieldPath + RAW_SOURCE + ": должно быть пустым");
            }
        }
        return errors;
    }
}
