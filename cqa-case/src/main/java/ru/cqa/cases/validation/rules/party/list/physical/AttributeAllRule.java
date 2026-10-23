package ru.cqa.cases.validation.rules.party.list.physical;

import ru.cqa.cases.validation.constants.AttributesTypes;
import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.AttributeDTO;
import ru.cqa.dto.PartyDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 18. Правило сохранения атрибутов — ОБЩАЯ проверка на ВСЕ атрибуты.
 *
 * <p>ЧАСТНАЯ проверка АТРИБУТОВ Физического лица.
 */
public class AttributeAllRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        List<AttributeDTO> attrs = party.attribute();
        if (attrs == null || attrs.isEmpty()) {
            return List.of();
        }
        boolean typesNull = attrs.stream().anyMatch(a -> a == null || isBlank(a.type()));
        String typesErr = attrs.stream()
                .filter(a -> a != null && !isBlank(a.type()) && !AttributesTypes.physicalValues.contains(a.type()))
                .map(AttributeDTO::type)
                .collect(Collectors.joining(", "));
        if (typesErr.isEmpty() && !typesNull) {
            return List.of();
        }
        if (typesErr.isEmpty()) {
            return List.of("party[0].attribute[i]. Недопустимы пустые и null значения типа Атрибута ФЛ");
        }
        return List.of("party[0].attribute[i]. Недопустимые типы для Атрибута ФЛ: " + typesErr
                + (typesNull ? ", а так же недопустимы пустые и null значения" : ""));
    }
}
