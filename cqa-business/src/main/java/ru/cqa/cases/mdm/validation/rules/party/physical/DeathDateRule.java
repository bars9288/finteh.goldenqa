package ru.cqa.cases.mdm.validation.rules.party.physical;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import ru.cqa.api.validation.Rule;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.PartyFieldNames.BIRTH_DATE_RAW_SOURCE;
import static ru.cqa.cases.mdm.constants.PartyFieldNames.DEATH_DATE;
import static ru.cqa.utils.DateValidationUtils.parseDateOrNull;

/**
 * 9. Дата смерти — только в {@code deathDate}, формат {@code ДД.ММ.ГГГГ}, позже даты рождения.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class DeathDateRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        Map<String, String> fieldsByName = FieldMaps.of(party);
        String deathDateValue = fieldsByName.get(DEATH_DATE);
        if (isBlank(deathDateValue)) {
            return List.of();
        }
        LocalDate deathDate = parseDateOrNull(deathDateValue.strip());
        if (deathDate == null) {
            return List.of("party[0].field." + DEATH_DATE + ": ожидается дата в формате ДД.ММ.ГГГГ");
        }
        String birthDateRawSource = fieldsByName.get(BIRTH_DATE_RAW_SOURCE);
        LocalDate birthDate = isBlank(birthDateRawSource) ? null : parseDateOrNull(birthDateRawSource.strip());
        if (birthDate != null && !deathDate.isAfter(birthDate)) {
            return List.of("party[0].field." + DEATH_DATE + ": дата смерти должна быть позже даты рождения");
        }
        return List.of();
    }
}
