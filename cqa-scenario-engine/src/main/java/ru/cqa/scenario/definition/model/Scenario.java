package ru.cqa.scenario.definition.model;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;


@Schema(description = "Декларативное описание сценария, его требований и пути/шаги исполнения")
public record Scenario(

        @Schema(description = "ИУникальный идентификатор сценария", example = "50e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID id,

        @Schema(description = "Номер версии сценария", example = "1.0.0", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String version,

        @Schema(description = "Мета-данные сценария", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Valid
        ScenarioMetadata metadata,

        @Schema(description = "Внешние зависимости, необходимые для выполнения сценария", requiredMode = Schema.RequiredMode.REQUIRED)
        List<@NotNull @Valid Dependency> dependencies,

        @Schema(description = "Параметры и правила выполнения сценария")
        @Valid Execution execution,

        @Schema(description = "Упорядоченная последовательность шагов сценария", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty List<@NotNull @Valid Step> steps) {
}
