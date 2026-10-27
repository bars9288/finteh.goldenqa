package ru.cqa.businesslogic.validation.rules.party.common;

import ru.cqa.businesslogic.validation.rules.Rule;
import ru.cqa.businesslogic.utils.FieldMaps;
import ru.cqa.dto.AttributeDTO;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;

import static ru.cqa.businesslogic.constants.RequisitesNames.ACTUALITY_DATE;
import static ru.cqa.businesslogic.utils.DateFormatUtils.checkDateTimeZnMsk;

/**
 * 6. Правило для атрибута(-ов) — ОБЩАЯ проверка: у каждого атрибута обязателен реквизит
 * {@code actualityDate}, формат {@code ДД.ММ.ГГГГ ЧЧ:мм:СС МСК}.
 * Пояс необязателен, но если указан — только московский.
 *
 * <p>Применяется для всех типов Party (Физическое лицо / Юридическое лицо).
 */
public final class ActualitydateAttrRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        List<AttributeDTO> attrs = party.attribute();
        if (attrs == null) {
            return List.of();
        }
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < attrs.size(); i++) {
            AttributeDTO a = attrs.get(i);
            if (a == null) {
                continue;
            }
            String path = "party[0].attribute[" + i + "].field. ";
            String raw = FieldMaps.of(a).get(ACTUALITY_DATE);
            if (isBlank(raw)) {
                errors.add(path + ACTUALITY_DATE + ": обязательно, не должно быть пустым");
            } else {
                errors.addAll(checkDateTimeZnMsk(raw.strip().split("\\s"),
                        path + ACTUALITY_DATE,
                        "ожидается дата и время в формате ДД.ММ.ГГГГ ЧЧ:мм:СС, после времени — необязательный пояс"));
            }
        }
        return errors;
    }
}
