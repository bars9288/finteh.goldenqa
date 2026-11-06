package ru.cqa.cases.mdm.validation.rules.party.physical;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ru.cqa.api.validation.Rule;
import ru.cqa.cases.mdm.constants.GenderValues;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.PartyFieldNames.GENDER;
import static ru.cqa.cases.mdm.constants.PartyFieldNames.GENDER_RAW_SOURCE;

/**
 * 10. Пол — только в {@code genderRawSource}, значения {@code MALE} или {@code FEMALE};
 * поле {@code gender} не заполняется.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class GenderRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        Map<String, String> fieldsByName = FieldMaps.of(party);
        List<String> errors = new ArrayList<>();
        if (!isBlank(fieldsByName.get(GENDER))) {
            errors.add("party[0].field." + GENDER + ": не заполняется, пол указывайте в " + GENDER_RAW_SOURCE);
        }
        String genderRawSource = fieldsByName.get(GENDER_RAW_SOURCE);
        if (!isBlank(genderRawSource) && !GenderValues.ALLOWED_VALUES.contains(genderRawSource.toUpperCase().strip())) {
            errors.add("party[0].field." + GENDER_RAW_SOURCE + ": допустимо MALE или FEMALE");
        }
        return errors;
    }
}
