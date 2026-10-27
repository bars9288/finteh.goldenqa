package ru.cqa.businesslogic.validation.rules.party.common;

import ru.cqa.businesslogic.validation.rules.Rule;
import ru.cqa.businesslogic.utils.FieldMaps;
import ru.cqa.dto.PartyDTO;

import java.util.List;

import static ru.cqa.businesslogic.constants.RequisitesNames.CREATED_DATE;
import static ru.cqa.businesslogic.utils.DateFormatUtils.checkDateTimeZnMsk;
import static ru.cqa.businesslogic.utils.DateFormatUtils.isDdMmYyyy;

/**
 * 2. ОБЩАЯ проверка реквизита: дата создания {@code createdDate} обязательна,
 * формат {@code ДД.ММ.ГГГГ} или {@code ДД.ММ.ГГГГ ЧЧ:мм:СС МСК}.
 * Пояс необязателен, но если указан — только московский.
 *
 * <p>Применяется для всех типов Party (Физическое лицо / Юридическое лицо).
 */
public final class CreateddateRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        String raw = FieldMaps.of(party).get(CREATED_DATE);
        if (isBlank(raw)) {
            return List.of("party[0].field." + CREATED_DATE + ": обязательно, не должно быть пустым");
        }
        String[] parts = raw.strip().split("\\s");
        if (parts.length == 1 && isDdMmYyyy(parts[0])) {
            return List.of();
        }
        return checkDateTimeZnMsk(parts, "party[0].field." + CREATED_DATE,
                "ожидается дата в формате ДД.ММ.ГГГГ или ДД.ММ.ГГГГ ЧЧ:мм:СС в Московском часовом поясе");
    }
}