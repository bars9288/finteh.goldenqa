package ru.cqa.cases.mdm.validation.rules.greeting;

import java.util.ArrayList;
import java.util.List;
import ru.cqa.api.validation.Rule;
import ru.cqa.model.greeting.OperatorDTO;

import static ru.cqa.cases.mdm.constants.FieldPatterns.RU_CAPITALIZED;

/**
 * Имя и фамилия оператора — только русские буквы, первая заглавная, остальные строчные.
 * Пустые значения не проверяются: о них уже сообщает {@code @NotBlank} в {@link OperatorDTO}.
 */
public final class OperatorNameRule implements Rule<OperatorDTO> {

    private static final String MESSAGE = ": только русские буквы, первая заглавная, остальные строчные";

    @Override
    public List<String> check(OperatorDTO operator) {
        List<String> errors = new ArrayList<>();
        checkCapitalized("name", operator.name(), errors);
        checkCapitalized("family", operator.family(), errors);
        return errors;
    }

    private void checkCapitalized(String field, String value, List<String> errors) {
        if (!isBlank(value) && !RU_CAPITALIZED.matcher(value).matches()) {
            errors.add(field + MESSAGE);
        }
    }
}
