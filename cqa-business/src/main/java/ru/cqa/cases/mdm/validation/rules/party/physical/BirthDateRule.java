package ru.cqa.cases.mdm.validation.rules.party.physical;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ru.cqa.api.validation.Rule;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.PartyFieldNames.BIRTH_DATE;
import static ru.cqa.cases.mdm.constants.PartyFieldNames.BIRTH_DATE_RAW_SOURCE;
import static ru.cqa.utils.DateValidationUtils.isValidDate;

/**
 * 8. Дата рождения в формате {@code ДД.ММ.ГГГГ} передаётся только в {@code birthdateRawSource}.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class BirthDateRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        Map<String, String> fieldsByName = FieldMaps.of(party);
        List<String> errors = new ArrayList<>();
        String birthDate = fieldsByName.get(BIRTH_DATE);
        if (!isBlank(birthDate)) {
            errors.add("party[0].field." + BIRTH_DATE + "не заполняется, дату указывайте в " + BIRTH_DATE_RAW_SOURCE + " в формате ДД.ММ.ГГГГ ");
        }
        String birthDateRawSource = fieldsByName.get(BIRTH_DATE_RAW_SOURCE);
        if (!isBlank(birthDateRawSource) && !isValidDate(birthDateRawSource.strip())) {
            errors.add("party[0].field." + BIRTH_DATE_RAW_SOURCE + ": ожидается дата в формате ДД.ММ.ГГГГ");
        }
        return errors;
    }
}
