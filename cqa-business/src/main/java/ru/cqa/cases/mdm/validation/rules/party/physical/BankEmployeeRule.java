package ru.cqa.cases.mdm.validation.rules.party.physical;

import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.cases.mdm.constants.BooleanFieldValues;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.utils.FieldMaps;

import static ru.cqa.cases.mdm.constants.PartyFieldNames.BANK_EMPLOYEE_FLAG;

/**
 * 17. Признак сотрудника банка — {@code TRUE}, {@code FALSE} или {@code UNKNOWN}; пустое допустимо.
 *
 * <p>ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица.
 */
public final class BankEmployeeRule implements Rule<PartyDTO> {

    @Override
    public List<String> check(PartyDTO party) {
        String bankEmployeeValue = FieldMaps.of(party).get(BANK_EMPLOYEE_FLAG);
        if (!isBlank(bankEmployeeValue) && !BooleanFieldValues.ALLOWED_VALUES.contains(bankEmployeeValue.strip())) {
            return List.of("party[0].field." + BANK_EMPLOYEE_FLAG + ": допустимо TRUE, FALSE или UNKNOWN");
        }
        return List.of();
    }

}
