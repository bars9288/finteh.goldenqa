package ru.cqa.business.validation.rules.party.physical;

import ru.cqa.business.validation.rules.Rule;
import ru.cqa.business.utils.FieldMaps;
import ru.cqa.dto.PartyDTO;

import java.util.List;

import static ru.cqa.business.constants.RequisitesNames.CITIZENSHIP;
import static ru.cqa.business.constants.RequisitesPatternRegex.CITIZENSHIP_FORMAT;
import static ru.cqa.business.constants.RequisitesPatternRegex.UNKNOWN;

/**
 * 12. Код гражданства — от одной до трёх цифр (2, 12, 123) или {@code UNKNOWN}; пустое допустимо.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class CitizenshipRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        String raw = FieldMaps.of(party).get(CITIZENSHIP);
        if (isBlank(raw)) {
            return List.of();
        }
        String value = raw.strip();
        if (!CITIZENSHIP_FORMAT.matcher(value).matches() && !UNKNOWN.equals(value)) {
            return List.of("party[0].field." + CITIZENSHIP + ": ожидается от одной до трёх цифр или UNKNOWN");
        }
        return List.of();
    }
}
