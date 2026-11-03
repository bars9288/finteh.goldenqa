package ru.cqa.scenario.definition.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Человекочитаемое описание сценария для отображения в интерфейсе")
public record ScenarioMetadata(

        @Schema(description = "Название сценария", example = "Проверка REST API", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String name,

        @Schema(description = "Описание сценария", example = "Проверка получения информации о пользователе", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String description,

        @Schema(description = "(Опционально) Категория сценария", example = "API Testing")
        String category,

        @Schema(description = "(Опционально) Описание назначения сценария", example = "[ \"http\", \"smoke\", \"regression\"]")
        List<@NotBlank String> tags) {
}
