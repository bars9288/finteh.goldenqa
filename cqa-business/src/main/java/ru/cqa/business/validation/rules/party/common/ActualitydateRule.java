package ru.cqa.business.validation.rules.party.common;

import ru.cqa.business.validation.rules.Rule;
import ru.cqa.business.utils.FieldMaps;
import ru.cqa.dto.PartyDTO;

import java.util.List;

import static ru.cqa.business.constants.RequisitesNames.ACTUALITY_DATE;
import static ru.cqa.business.utils.DateFormatUtils.checkDateTimeZnMsk;

/**
 * 3. ОБЩАЯ проверка реквизитов: дата актуальности {@code actualityDate} обязательна,
 * формат {@code ДД.ММ.ГГГГ ЧЧ:мм:СС МСК}. Пояс необязателен, но если указан — только московский.
 *
 * <p>Применяется для всех типов Party (Физическое лицо / Юридическое лицо).
 */
public final class ActualitydateRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        String raw = FieldMaps.of(party).get(ACTUALITY_DATE);
        if (isBlank(raw)) {
            return List.of("party[0].field." + ACTUALITY_DATE + ": обязательно, не должно быть пустым");
        }
        return checkDateTimeZnMsk(raw.strip().split("\\s"),
                "party[0].field. " + ACTUALITY_DATE,
                "ожидается дата и время в формате ДД.ММ.ГГГГ ЧЧ:мм:СС, после времени — необязательный пояс");
    }
}
