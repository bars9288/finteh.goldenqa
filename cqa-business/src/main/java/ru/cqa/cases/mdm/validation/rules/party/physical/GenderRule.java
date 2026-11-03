package ru.cqa.cases.mdm.validation.rules.party.physical;

import ru.cqa.cases.mdm.constants.RequisitesGenderFields;
import ru.cqa.validation.Rule;
import ru.cqa.utils.FieldMaps;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.cqa.cases.mdm.constants.RequisitesNames.GENDER;
import static ru.cqa.cases.mdm.constants.RequisitesNames.GENDER_RAW_SOURCE;

/**
 * 10. Пол — только в {@code genderRawSource}, значения {@code MALE} или {@code FEMALE};
 * поле {@code gender} не заполняется.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class GenderRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        Map<String, String> f = FieldMaps.of(party);
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
