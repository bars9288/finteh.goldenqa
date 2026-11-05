package ru.cqa.scenario.runtime.result;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

@Schema(description = "Результаты выполнения операций и общий статус одного шага сценария")
public record StepResult(

        @Schema(description = "Идентификатор выполненного шага", example = "step-001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String stepId,

        @Schema(description = "Итоговый статус выполнения шага", example = "COMPLETED", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        StepStatus status,

        @Schema(description = "Список результатов операций, выполненных в рамках шага", nullable = true)
        List<@NotNull @Valid MoveResult> moves,

        @Schema(description = "Общее время выполнения шага в миллисекундах", example = "1250", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        @PositiveOrZero
        long executionTimeMs) {
}
