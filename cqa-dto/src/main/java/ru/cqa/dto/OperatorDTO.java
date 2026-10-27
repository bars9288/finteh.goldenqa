package ru.cqa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Оператор, от имени которого выполняется проверка")
public record OperatorDTO(

        @Schema(description = "Имя оператора", example = "Сергей", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Не должно быть пустым")
        String name,

        @Schema(description = "Фамилия оператора", example = "Борискин", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Не должно быть пустым")
        String family) {
}
