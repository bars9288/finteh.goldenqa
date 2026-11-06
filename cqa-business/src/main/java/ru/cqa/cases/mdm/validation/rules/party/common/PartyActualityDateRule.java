package ru.cqa.cases.mdm.validation.rules.party.common;

import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.PartyFieldNames.ACTUALITY_DATE;
import static ru.cqa.utils.DateValidationUtils.validateDateTimeWithMoscowZone;

/**
 * 3. ОБЩАЯ проверка реквизитов: дата актуальности {@code actualityDate} обязательна,
 * формат {@code ДД.ММ.ГГГГ ЧЧ:мм:СС МСК}. Пояс необязателен, но если указан — только московский.
 *
 * <p>Применяется для всех типов Party (Физическое лицо / Юридическое лицо).
 */
public final class PartyActualityDateRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        String actualityDateValue = FieldMaps.of(party).get(ACTUALITY_DATE);
        if (isBlank(actualityDateValue)) {
            return List.of("party[0].field." + ACTUALITY_DATE + ": обязательно, не должно быть пустым");
        }
        return validateDateTimeWithMoscowZone(actualityDateValue.strip().split("\\s"),
                "party[0].field. " + ACTUALITY_DATE,
                "ожидается дата и время в формате ДД.ММ.ГГГГ ЧЧ:мм:СС, после времени — необязательный пояс");
    }
}
