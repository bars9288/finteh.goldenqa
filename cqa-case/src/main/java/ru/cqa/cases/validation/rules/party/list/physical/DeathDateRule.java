package ru.cqa.cases.validation.rules.party.list.physical;

import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static ru.cqa.cases.validation.constants.RequisitesNames.BIRTHDATE_RAW_SOURCE;
import static ru.cqa.cases.validation.constants.RequisitesNames.DEATH_DATE;
import static ru.cqa.cases.validation.utils.DateFormatUtils.parseDdMmYyyy;

public class DeathDateRule implements Rule<PartyDTO> {

    // ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица
    //9. Дата смерти — только в deathDate, формат ДД.ММ.ГГГГ, позже даты рождения

    @Override
    public List<String> check(PartyDTO party) {
        Map<String, String> f = party.fieldMap();
        String raw = f.get(DEATH_DATE);
        if (isBlank(raw)) {
            return List.of();
        }
        LocalDate death = parseDdMmYyyy(raw.strip());
        if (death == null) {
            return List.of("party[0].field." + DEATH_DATE + ": ожидается дата в формате ДД.ММ.ГГГГ");
        }
        String birthRaw = f.get(BIRTHDATE_RAW_SOURCE);
        LocalDate birth = isBlank(birthRaw) ? null : parseDdMmYyyy(birthRaw.strip());
        if (birth != null && !death.isAfter(birth)) {
            return List.of("party[0].field." + DEATH_DATE + ": дата смерти должна быть позже даты рождения");
        }
        return List.of();
    }
}
