package ru.cqa.model.assertion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Результат выполнения одной проверки Assertion")
public record AssertionResult(

        @Schema(description = "Идентификатор выполненной проверки", example = "assert-user-status", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String assertionId,

        @Schema(description = "Статус выполнения проверки", example = "PASSED", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        AssertionStatus status,

        @Schema(description = "Сообщение с диагностикой результата проверки", example = "Ожидаемое и фактическое значения совпадают")
        String message) {
}
