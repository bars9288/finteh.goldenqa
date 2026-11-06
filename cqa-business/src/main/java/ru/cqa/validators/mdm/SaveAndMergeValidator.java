package ru.cqa.validators.mdm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ru.cqa.api.validation.FormValidator;
import ru.cqa.api.validation.Rule;
import ru.cqa.api.validation.SaveAndMergeValidatorException;
import ru.cqa.api.validation.Validator;
import ru.cqa.model.mdm.PartyDTO;
import ru.cqa.model.mdm.SaveAndMergeDTO;

/** Проверяет форму и предметные правила, собирая все нарушения в один список. */
public final class SaveAndMergeValidator implements Validator<SaveAndMergeDTO> {
    private final FormValidator formValidator;
    private final List<Rule<PartyDTO>> rules;

    public SaveAndMergeValidator(FormValidator formValidator, List<Rule<PartyDTO>> rules) {
        this.formValidator = Objects.requireNonNull(formValidator, "formValidator");
        this.rules = List.copyOf(rules);
    }

    @Override
    public void validateBody(SaveAndMergeDTO request) {
        List<String> errors = new ArrayList<>(formValidator.validate(request));
        List<PartyDTO> parties = request.party();
        PartyDTO entity = parties == null || parties.isEmpty() ? null : parties.get(0);
        if (entity != null) {
            rules.forEach(rule -> errors.addAll(rule.check(entity)));
        }
        if (!errors.isEmpty()) {
            throw new SaveAndMergeValidatorException(errors);
        }
    }
}
