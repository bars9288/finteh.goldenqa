package ru.cqa.cases.mdm.validation.rules.party.physical;

import java.util.List;
import java.util.stream.Collectors;
import ru.cqa.api.validation.Rule;
import ru.cqa.cases.mdm.constants.AttributeTypes;
import ru.cqa.model.mdm.AttributeDTO;
import ru.cqa.model.mdm.PartyDTO;

/**
 * 18. Правило сохранения атрибутов — ОБЩАЯ проверка на ВСЕ атрибуты.
 *
 * <p>ЧАСТНАЯ проверка АТРИБУТОВ Физического лица.
 */
public final class PhysicalPartyAttributeTypeRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        List<AttributeDTO> attributes = party.attribute();
        if (attributes == null || attributes.isEmpty()) {
            return List.of();
        }
        boolean hasMissingType = attributes.stream().anyMatch(attribute -> attribute == null || isBlank(attribute.type()));
        String unsupportedTypes = attributes.stream()
                .filter(attribute -> attribute != null && !isBlank(attribute.type()) && !AttributeTypes.PHYSICAL_PARTY_TYPES.contains(attribute.type()))
                .map(AttributeDTO::type)
                .collect(Collectors.joining(", "));
        if (unsupportedTypes.isEmpty() && !hasMissingType) {
            return List.of();
        }
        if (unsupportedTypes.isEmpty()) {
            return List.of("party[0].attribute[i]. Недопустимы пустые и null значения типа Атрибута ФЛ");
        }
        return List.of("party[0].attribute[i]. Недопустимые типы для Атрибута ФЛ: " + unsupportedTypes
                + (hasMissingType ? ", а так же недопустимы пустые и null значения" : ""));
    }
}
