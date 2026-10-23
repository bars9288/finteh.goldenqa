package ru.cqa.cases.validation;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import ru.cqa.dto.PartyDTO;
import ru.cqa.dto.SaveAndMergeDTO;

/** Проверка входящего запроса saveAndMerge. Два слоя, ошибки обоих собираются в один список:
 *   <li>форма — аннотации DTO ({@code @NotBlank}, {@code @NotEmpty}...), по всему дереву объектов разом;</li>
 *   <li>правила — проверки, которые аннотацией не выразить, список {@link PhysicalRule}.</li>
 * Если список не пуст, бросается {@link SaveAndMergeValidationException}.
 */
public class SaveAndMergeValidator {

    private final Validator validator;
    private final List<PhysicalRule> rules;

    public SaveAndMergeValidator(Validator validator, List<PhysicalRule> rules) {
        this.validator = validator;
        this.rules = List.copyOf(rules);
    }

    public void validate(SaveAndMergeDTO request) {
        List<String> errors = new ArrayList<>();
        checkForm(request, errors);
        List<PartyDTO> parties = request.party();
        if (parties != null && !parties.isEmpty() && parties.get(0) != null) {
            checkRules(parties.get(0), errors);
        }
        if (!errors.isEmpty()) {
            throw new SaveAndMergeValidationException(errors);
        }
    }

    /** Нарушения аннотаций в виде «путь: сообщение», например {@code party[0].type: ...}. */
    private void checkForm(SaveAndMergeDTO request, List<String> errors) {
        validator.validate(request).stream()
                .map(SaveAndMergeValidator::describe)
                .sorted()
                .forEach(errors::add);
    }

    private void checkRules(PartyDTO party, List<String> errors) {
        rules.forEach(rule -> errors.addAll(rule.checkPhysical(party)));
    }

    private static String describe(ConstraintViolation<SaveAndMergeDTO> violation) {
        // элемент списка Hibernate Validator помечает служебным «.<list element>» — клиенту он не нужен
        String path = violation.getPropertyPath().toString().replace(".<list element>", "");
        return path + ": " + violation.getMessage();
    }
}
