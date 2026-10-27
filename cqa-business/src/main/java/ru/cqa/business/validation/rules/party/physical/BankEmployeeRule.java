package ru.cqa.business.validation.rules.party.physical;

import ru.cqa.business.constants.RequisBoolFields;
import ru.cqa.business.validation.rules.Rule;
import ru.cqa.business.utils.FieldMaps;
import ru.cqa.dto.PartyDTO;

import java.util.List;

import static ru.cqa.business.constants.RequisitesNames.BANK_EMPLOYEE_FLAG;

/**
 * 17. Признак сотрудника банка — {@code TRUE}, {@code FALSE} или {@code UNKNOWN}; пустое допустимо.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class BankEmployeeRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        String raw = FieldMaps.of(party).get(BANK_EMPLOYEE_FLAG);
        if (!isBlank(raw) && !RequisBoolFields.BoolValues.contains(raw.strip())) {
            return List.of("party[0].field." + BANK_EMPLOYEE_FLAG + ": допустимо TRUE, FALSE или UNKNOWN");
        }
        return List.of();
    }

}
