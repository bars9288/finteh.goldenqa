package ru.cqa.server.web.greeting;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Тело POST-запроса к /helloAkBars.
 *
 * <p>DTO транспортного слоя: поля могут прийти пустыми или не прийти вовсе,
 * поэтому здесь нет проверок — за корректность отвечает доменная модель.
 */
public record HelloRequest(
        @Schema(description = "Имя оператора", example = "Сергей") String name,
        @Schema(description = "Фамилия оператора", example = "Борискин") String family) {

    /** Значение поля или {@code fallback}, если в теле запроса его не было. */
    public String nameOr(String fallback) {
        return name != null ? name : fallback;
    }

    public String familyOr(String fallback) {
        return family != null ? family : fallback;
    }
}
