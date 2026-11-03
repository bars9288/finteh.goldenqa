package ru.cqa.validators;

import jakarta.validation.ConstraintViolation;
import ru.cqa.cases.mdm.exceptions.ValidationException;
import ru.cqa.cases.mdm.validation.rules.Rule;

import java.util.ArrayList;
import java.util.List;

/**  Проверка входящего запроса.
 *  Два слоя, ошибки обоих собираются в один список:
 *   <li>форма — аннотации DTO ({@code @NotBlank}, {@code @NotEmpty}...) по всему запросу {@code D};</li>
 *   <li>правила — проверки, которые аннотацией не выразить, список {@link Rule} по сущности {@code T}.</li>
 *
 * @param <D> ДТО запроса Request целиком, который проверяется аннотациями
 * @param <T> ДТО сущности из запроса, по которой идут правила (может быть {@code <D>})
 */
public abstract class Validator<D, T, E extends ValidationException> {

    private final jakarta.validation.Validator validator;
    private final List<Rule<T>> rules;

    /* вернуть null, если сущности нет — тогда правила List<Rule<T>> не запускаются */
    protected abstract T entity(D request);

    protected abstract E exception(List<String> errors);

    protected Validator(jakarta.validation.Validator validator, List<Rule<T>> rules) {
        this.validator = validator;
        this.rules = List.copyOf(rules);
    }

    protected final void checkForm(D requestDTO, List<String> errors) {
        validator.validate(requestDTO).stream()
                .map(Validator::describe)
                .sorted()
                .forEach(errors::add);
    }

    protected final void checkRules(T entityDTO, List<String> errors) {
        rules.forEach(rule -> errors.addAll(rule.check(entityDTO)));
    }

    public final void validateBody(D request) {
        List<String> errors = new ArrayList<>();
        checkForm(request, errors);
        T entityDTO = entity(request);
        if (entityDTO != null) {
            checkRules(entityDTO, errors);
        }
        if (!errors.isEmpty()) {
            throw exception(errors);
        }
    }

    private static String describe(ConstraintViolation<?> violation) {
        // элемент списка Hibernate Validator помечает служебным «.<list element>» — клиенту он не нужен
        String path = violation.getPropertyPath().toString().replace(".<list element>", "");
        return path + ": " + violation.getMessage();
    }
}
