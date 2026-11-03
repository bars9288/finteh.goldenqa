package ru.cqa.scenario.definition.model;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Описание внешней зависимости сценария")
public record Dependency(

        @Schema(description = "Идентификатор настроенного подключения", example = "mdm-rest-api-connection", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String connectionId,

        @Schema(description = "Тип внешнего подключения или транспорт", example = "http", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String type,

        @Schema(description = "Признак обязательности зависимости для выполнения сценария", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean required,

        @Schema(description = "Пояснение назначения зависимости", requiredMode = Schema.RequiredMode.REQUIRED)
        String description) {
}
