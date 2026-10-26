package ru.cqa.cases.validation.rules.party.list.physical;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.cqa.cases.validation.constants.RequisitesNames.BIRTHDATE;
import static ru.cqa.cases.validation.constants.RequisitesNames.BIRTHDATE_RAW_SOURCE;
import static ru.cqa.cases.validation.utils.DateFormatUtils.isDdMmYyyy;

/**
 * 8. Дата рождения в формате {@code ДД.ММ.ГГГГ} передаётся только в {@code birthdateRawSource}.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class BirthdayDateRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        Map<String, String> f = party.fieldMap();
        List<String> errors = new ArrayList<>();
        String birthdate = f.get(BIRTHDATE);
        if (!isBlank(birthdate)) {
            errors.add("party[0].field." + BIRTHDATE + "не заполняется, дату указывайте в " + BIRTHDATE_RAW_SOURCE + " в формате ДД.ММ.ГГГГ ");
        }
        String raw = f.get(BIRTHDATE_RAW_SOURCE);
        if (!isBlank(raw) && !isDdMmYyyy(raw.strip())) {
            errors.add("party[0].field." + BIRTHDATE_RAW_SOURCE + ": ожидается дата в формате ДД.ММ.ГГГГ");
        }
        return errors;
    }
}
