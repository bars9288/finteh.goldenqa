package ru.cqa.cases.saveAndMerge;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import ru.cqa.dto.SaveAndMergeDTO;

/**
 * Проверка входящего запроса saveAndMerge.
 *
 * <p>Два слоя, ошибки обоих собираются в один список:
 * <ol>
 *   <li>форма — аннотации DTO ({@code @NotBlank}, {@code @NotEmpty}...),
 *       по всему дереву объектов разом;</li>
 *   <li>правила — проверки, которые аннотацией не выразить (пока заглушка).</li>
 * </ol>
 * Если список не пуст, бросается {@link SaveAndMergeValidationException}.
 *
 * <p>{@link Validator} — только интерфейс Bean Validation; движок передаёт
 * снаружи cqa-server, поэтому модуль остаётся без Spring.
 */
public class SaveAndMergeValidator {

    private final Validator validator;

    public SaveAndMergeValidator(Validator validator) {
        this.validator = validator;
    }

    public void validate(SaveAndMergeDTO request) {
        List<String> errors = new ArrayList<>();
        checkForm(request, errors);
        checkRules(request, errors);
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

    /** Заглушка: сюда добавляются правила проверки, каждое пишет свои ошибки в {@code errors}. */
    private void checkRules(SaveAndMergeDTO request, List<String> errors) {
    }

    private static String describe(ConstraintViolation<SaveAndMergeDTO> violation) {
        return violation.getPropertyPath() + ": " + violation.getMessage();
    }
}
