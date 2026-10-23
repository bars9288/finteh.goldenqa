package ru.cqa.cases.validation.rules.party.list.physical;

import ru.cqa.cases.validation.constants.RequisBoolFields;
import ru.cqa.cases.validation.rules.Rule;
import ru.cqa.dto.PartyDTO;

import java.util.List;

import static ru.cqa.cases.validation.constants.RequisitesNames.BANK_EMPLOYEE_FLAG;

public class BankEmployeeRule implements Rule<PartyDTO> {

    // ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица
    //17. Признак сотрудника банка — TRUE, FALSE или UNKNOWN; пустое допустимо

    @Override
    public List<String> check(PartyDTO party) {
        String raw = party.fieldMap().get(BANK_EMPLOYEE_FLAG);
        if (!isBlank(raw) && !RequisBoolFields.BoolValues.contains(raw.strip())) {
            return List.of("party[0].field." + BANK_EMPLOYEE_FLAG + ": допустимо TRUE, FALSE или UNKNOWN");
        }
        return List.of();
    }

}
