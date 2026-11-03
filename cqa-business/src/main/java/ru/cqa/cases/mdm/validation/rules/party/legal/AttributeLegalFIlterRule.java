package ru.cqa.cases.mdm.validation.rules.party.legal;

import ru.cqa.cases.mdm.constants.AttributesTypes;
import ru.cqa.cases.mdm.validation.rules.Rule;
import ru.cqa.dto.AttributeDTO;
import ru.cqa.dto.PartyDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 31. Правило сохранения атрибутов — ОБЩАЯ проверка на ВСЕ атрибуты.
 *
 * <p>ЧАСТНАЯ проверка АТРИБУТОВ Юридического лица.
 */
public final class AttributeLegalFIlterRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        List<AttributeDTO> attrs = party.attribute();
        if (attrs == null || attrs.isEmpty()) {
            return List.of();
        }
        boolean typesNull = attrs.stream().anyMatch(a -> a == null || isBlank(a.type()));
        String typesErr = attrs.stream()
                .filter(a -> a != null && !isBlank(a.type()) && !AttributesTypes.legalValues.contains(a.type()))
                .map(AttributeDTO::type)
                .collect(Collectors.joining(", "));
        if (typesErr.isEmpty() && !typesNull) {
            return List.of();
        }
        if (typesErr.isEmpty()) {
            return List.of("party[0].attribute[i]. Недопустимы пустые и null значения типа Атрибута ЮЛ");
        }
        return List.of("party[0].attribute[i]. Недопустимые типы для Атрибута ЮЛ: " + typesErr
                + (typesNull ? ", а так же недопустимы пустые и null значения" : ""));
    }
}
