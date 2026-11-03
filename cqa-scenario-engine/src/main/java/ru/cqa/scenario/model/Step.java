package ru.cqa.scenario.model;

import java.util.List;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Отдельный шаг сценария, содержащий последовательность операций")
public record Step(

        @Schema(description = "Идентификатор шага", example = "step-001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String id,

        @Schema(description = "Название шага", example = "Получение пользователя", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String name,

        @Schema(description = "Описание выполняемой последовательности операций", example = "Отправка запроса и проверка ответа")
        String description,

        @Schema(description = "Продолжать ли выполнение сценария при ошибке этого шага", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean continueOnError,

        @Schema(description = "Операции, выполняемые в рамках шага", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty List<@NotNull @Valid Move> moves) {
}
