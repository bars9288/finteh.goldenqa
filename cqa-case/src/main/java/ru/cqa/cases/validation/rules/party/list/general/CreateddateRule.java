package ru.cqa.cases.validation.rules.party.list.general;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;

import java.util.List;

import static ru.cqa.cases.validation.constants.RequisitesNames.CREATED_DATE;
import static ru.cqa.cases.validation.utils.DateFormatUtils.checkDateTimeZnMsk;
import static ru.cqa.cases.validation.utils.DateFormatUtils.isDdMmYyyy;

public class CreateddateRule implements Rule<PartyDTO> {

    //2. ОБЩАЯ проверка реквизита: Дата создания — createdDate обязательна, формат ДД.ММ.ГГГГ или ДД.ММ.ГГГГ ЧЧ:мм:СС МСК;
    // пояс необязателен, но если указан — только московский
    //   Применяется для всех типов Party (Физическое лицо / Юридическое лицо)

    @Override
    public List<String> check(PartyDTO party) {
        String raw = party.fieldMap().get(CREATED_DATE);
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