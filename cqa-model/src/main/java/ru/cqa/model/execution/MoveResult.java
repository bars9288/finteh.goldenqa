package ru.cqa.model.execution;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import ru.cqa.model.assertion.AssertionResult;

@Schema(description = "Результат выполнения одной операции сценария")
public record MoveResult(

        @Schema(description = "Идентификатор выполненной операции", example = "move-001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String moveId,

        @Schema(description = "Статус выполнения операции", example = "COMPLETED", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        MoveStatus status,

        @Schema(description = "Результат выполнения операции. Может содержать HTTP-ответ, результат SQL-запроса или данные другого типа", nullable = true)
        Object response,

        @Schema(description = "Описание ошибки, возникшей при выполнении операции", example = "Connection timeout", nullable = true)
        String errorMessage,

        @Schema(description = "Время выполнения операции в миллисекундах", example = "245", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        @PositiveOrZero
        long executionTimeMs,

        @Schema(description = "Результаты проверок ответа этого Move")
        List<@NotNull @Valid AssertionResult> assertions) {

    public MoveResult(String moveId, MoveStatus status, Object response, String errorMessage, long executionTimeMs) {
        this(moveId, status, response, errorMessage, executionTimeMs, List.of());
    }

}
