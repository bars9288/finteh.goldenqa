package ru.cqa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import ru.cqa.business.greeting.operator.Operator;

/**
 * Тело POST-запроса к /helloAkBars.
 *
 * <p>Проверяет только форму запроса: оба поля пришли и не пустые. Это
 * срабатывает до вызова контроллера и даёт клиенту 400 со списком полей.
 * Бизнес-правила остаются в {@link Operator} — его конструктор проверяет
 * данные независимо от того, откуда они пришли.
 *
 * <p>{@code @NotBlank} уже отвергает {@code null}, поэтому {@code @NotNull}
 * рядом не нужен: он лишь продублировал бы ошибку на то же поле.
 */
@Schema(description = "Оператор, от имени которого выполняется проверка")
public record OperatorDTO(
        @Schema(description = "Имя оператора", example = "Сергей", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String name,
        @Schema(description = "Фамилия оператора", example = "Борискин", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String family) {

    /** Доменный объект из тела запроса. */
    public Operator toOperator() {
        return new Operator(name, family);
    }
}
