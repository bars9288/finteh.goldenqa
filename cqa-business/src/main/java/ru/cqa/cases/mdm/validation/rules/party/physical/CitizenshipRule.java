package ru.cqa.cases.mdm.validation.rules.party.physical;

import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.PartyFieldNames.CITIZENSHIP;
import static ru.cqa.cases.mdm.constants.FieldPatterns.CITIZENSHIP_FORMAT;
import static ru.cqa.cases.mdm.constants.FieldPatterns.UNKNOWN;

/**
 * 12. Код гражданства — от одной до трёх цифр (2, 12, 123) или {@code UNKNOWN}; пустое допустимо.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class CitizenshipRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        String citizenshipValue = FieldMaps.of(party).get(CITIZENSHIP);
        if (isBlank(citizenshipValue)) {
            return List.of();
        }
        String value = citizenshipValue.strip();
        if (!CITIZENSHIP_FORMAT.matcher(value).matches() && !UNKNOWN.equals(value)) {
            return List.of("party[0].field." + CITIZENSHIP + ": ожидается от одной до трёх цифр или UNKNOWN");
        }
        return List.of();
    }
}
