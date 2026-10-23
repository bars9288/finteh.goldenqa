package ru.cqa.cases.validation.rules.party.list.physical;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.cqa.cases.validation.constants.RequisitesNames.SNILS;
import static ru.cqa.cases.validation.constants.RequisitesNames.SNILS_RAW_SOURCE;
import static ru.cqa.cases.validation.constants.RequisitesPatternRegex.SNILS_FORMAT;

/**
 * 15. СНИЛС — только в {@code snilsRawSource}, формат {@code 123-123-123 12};
 * поле {@code snils} не заполняется.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public class SnilsRule  implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        Map<String, String> f = party.fieldMap();
        List<String> errors = new ArrayList<>();
        if (!isBlank(f.get(SNILS))) {
            errors.add("party[0].field." + SNILS + ": не заполняется, СНИЛС указывайте в " + SNILS_RAW_SOURCE);
        }
        String raw = f.get(SNILS_RAW_SOURCE);
        if (!isBlank(raw) && !SNILS_FORMAT.matcher(raw.strip()).matches()) {
            errors.add("party[0].field." + SNILS_RAW_SOURCE + ": ожидается формат \'123-123-123 12\'");
        }
        return errors;
    }
}
