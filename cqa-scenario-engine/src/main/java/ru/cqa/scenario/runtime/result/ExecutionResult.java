package ru.cqa.scenario.runtime.result;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;
import java.util.UUID;

@Schema(description = "Итог выполнения сценария с результатами всех завершённых шагов")
public record ExecutionResult(

        @Schema(description = "Уникальный идентификатор запуска сценария", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID executionId,

        @Schema(description = "Идентификатор выполненного сценария", example = "550e8400-e29b-41d4-a716-446655440001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID scenarioId,

        @Schema(description = "Итоговый статус выполнения сценария", example = "COMPLETED", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        ExecutionStatus status,

        @Schema(description = "Список результатов выполненных шагов сценария")
        List<@NotNull @Valid StepResult> steps,

        @Schema(description = "Общее время выполнения сценария в миллисекундах", example = "3500", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        @PositiveOrZero
        long executionTimeMs) {

}
