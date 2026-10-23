package ru.cqa.cases.validation.rules.party.list.physical;

import ru.cqa.cases.validation.constants.RequisitesGenderFields;
import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.cqa.cases.validation.constants.RequisitesNames.GENDER;
import static ru.cqa.cases.validation.constants.RequisitesNames.GENDER_RAW_SOURCE;

public class GenderRule implements Rule<PartyDTO> {

    // ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица
    //10. Пол — только в genderRawSource, значения MALE или FEMALE; поле gender не заполняется

    @Override
    public List<String> check(PartyDTO party) {
        Map<String, String> f = party.fieldMap();
        List<String> errors = new ArrayList<>();
        if (!isBlank(f.get(GENDER))) {
            errors.add("party[0].field." + GENDER + ": не заполняется, пол указывайте в " + GENDER_RAW_SOURCE);
        }
        String raw = f.get(GENDER_RAW_SOURCE);
        if (!isBlank(raw) && !RequisitesGenderFields.values.contains(raw.toUpperCase().strip())) {
            errors.add("party[0].field." + GENDER_RAW_SOURCE + ": допустимо MALE или FEMALE");
        }
        return errors;
    }
}
