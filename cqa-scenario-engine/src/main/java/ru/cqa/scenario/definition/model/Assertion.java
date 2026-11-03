
package ru.cqa.scenario.definition.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Условие проверки результата выполнения операции")
public record Assertion(

        @Schema(description = "Уникальный идентификатор проверки", example = "assert-status", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String id,

        @Schema(description = "Тип проверки, определяющий способ сравнения", example = "equals", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String type,

        @Schema(description = "JSONPath-выражение для получения фактического значения", example = "$.status", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String actualExpression,

        @Schema(description = "Ожидаемое значение, с которым сравнивается фактическое", example = "SUCCESS", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Object expectedValue) {
}
