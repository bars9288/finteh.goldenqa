package ru.cqa.cases.validation.rules.party.list.general;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;

import java.util.List;

import static ru.cqa.cases.validation.constants.RequisitesNames.ACTUALITY_DATE;
import static ru.cqa.cases.validation.utils.DateFormatUtils.checkDateTimeZnMsk;

/**
 * 3. ОБЩАЯ проверка реквизитов: дата актуальности {@code actualityDate} обязательна,
 * формат {@code ДД.ММ.ГГГГ ЧЧ:мм:СС МСК}. Пояс необязателен, но если указан — только московский.
 *
 * <p>Применяется для всех типов Party (Физическое лицо / Юридическое лицо).
 */
public final class ActualitydateRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        String raw = party.fieldMap().get(ACTUALITY_DATE);
        if (isBlank(raw)) {
            return List.of("party[0].field." + ACTUALITY_DATE + ": обязательно, не должно быть пустым");
        }
        return checkDateTimeZnMsk(raw.strip().split("\\s"),
                "party[0].field. " + ACTUALITY_DATE,
                "ожидается дата и время в формате ДД.ММ.ГГГГ ЧЧ:мм:СС, после времени — необязательный пояс");
    }
}
