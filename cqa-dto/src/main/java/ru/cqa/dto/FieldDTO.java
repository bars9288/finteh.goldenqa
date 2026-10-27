package ru.cqa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;


@Schema(description = "Поле: имя и значение")
public record FieldDTO(

        @Schema(description = "Имя поля", example = "surname", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Не должно быть пустым")
        String name,

        @Schema(description = "Значение поля", example = "Иванов")
        String value) {
}
