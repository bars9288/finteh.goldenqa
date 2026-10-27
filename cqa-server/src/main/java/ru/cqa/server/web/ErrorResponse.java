package ru.cqa.server.web;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/** Ответ с ошибкой проверки или вызова внешней системы: итог и список ошибок. */
public record ErrorResponse(

        @Schema(description = "Итог", example = "Ошибка")
        String message,

        @Schema(description = "Все найденные ошибки")
        List<String> errors) {

    public static ErrorResponse failed(List<String> errors) {
        return new ErrorResponse("Ошибка", errors);
    }
}
