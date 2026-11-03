package ru.cqa.scenario.model;

import java.time.Duration;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Данные о прерывании исполненя сценария и отработке ошибок")
public record Execution(

        @Schema(description = "Максимальная продолжительность выполнения сценария", type = "string", format = "duration",
                example = "PT30S", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Duration timeout,

        @Schema(description = "Продолжать выполнение следующих шагов при ошибке текущего шага", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean continueAfterFailure) {
}
