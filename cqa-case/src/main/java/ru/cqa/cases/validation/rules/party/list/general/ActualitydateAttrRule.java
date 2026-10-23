package ru.cqa.cases.validation.rules.party.list.general;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.AttributeDTO;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;

import static ru.cqa.cases.validation.constants.RequisitesNames.ACTUALITY_DATE;
import static ru.cqa.cases.validation.utils.DateFormatUtils.checkDateTimeZnMsk;

public class ActualitydateAttrRule implements Rule<PartyDTO> {

    //6. Правило для атрибута(-ов) - ОБЩАЯ проверка: у каждого атрибута обязателен реквизит actualityDate,
    //    формат ДД.ММ.ГГГГ ЧЧ:мм:СС МСК; пояс необязателен, но если указан — только московский
    //   Применяется для всех типов Party (Физическое лицо / Юридическое лицо)

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
            String raw = a.fieldMap().get(ACTUALITY_DATE);
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
