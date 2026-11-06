package ru.cqa.cases.mdm.validation.rules.party.common;

import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.PartyFieldNames.CREATED_DATE;
import static ru.cqa.utils.DateValidationUtils.validateDateTimeWithMoscowZone;
import static ru.cqa.utils.DateValidationUtils.isValidDate;

/**
 * 2. ОБЩАЯ проверка реквизита: дата создания {@code createdDate} обязательна,
 * формат {@code ДД.ММ.ГГГГ} или {@code ДД.ММ.ГГГГ ЧЧ:мм:СС МСК}.
 * Пояс необязателен, но если указан — только московский.
 *
 * <p>Применяется для всех типов Party (Физическое лицо / Юридическое лицо).
 */
public final class PartyCreatedDateRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        String createdDateValue = FieldMaps.of(party).get(CREATED_DATE);
        if (isBlank(createdDateValue)) {
            return List.of("party[0].field." + CREATED_DATE + ": обязательно, не должно быть пустым");
        }
        String[] dateTimeParts = createdDateValue.strip().split("\\s");
        if (dateTimeParts.length == 1 && isValidDate(dateTimeParts[0])) {
            return List.of();
        }
        return validateDateTimeWithMoscowZone(dateTimeParts, "party[0].field." + CREATED_DATE,
                "ожидается дата в формате ДД.ММ.ГГГГ или ДД.ММ.ГГГГ ЧЧ:мм:СС в Московском часовом поясе");
    }
}
