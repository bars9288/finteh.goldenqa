package ru.cqa.server.web.saveAndMerge;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/** Ответ ручки /saveAndMerge: итог проверки и список ошибок (пустой, если их нет). */
public record SaveAndMergeResponse(
        @Schema(description = "Итог проверки", example = "ОШИБОК НЕТ") String message,
        @Schema(description = "Все найденные ошибки") List<String> errors) {

    public static SaveAndMergeResponse ok() {
        return new SaveAndMergeResponse("ОШИБОК НЕТ", List.of());
    }

    public static SaveAndMergeResponse failed(List<String> errors) {
        return new SaveAndMergeResponse("Ошибка", errors);
    }
}
