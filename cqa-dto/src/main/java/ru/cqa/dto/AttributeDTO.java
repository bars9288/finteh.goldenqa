package ru.cqa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;


@Schema(description = "Атрибут участника")
public record AttributeDTO(

        @Schema(description = "Тип атрибута", example = "ADDRESS", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "не должно быть пустым")
        String type,

        @Schema(description = "Идентификатор строки", example = "12")
        @NotBlank(message = "не должно быть пустым")
        String rawId,

        @Schema(description = "Поля атрибута")
        List<@NotNull @NotEmpty @Valid FieldDTO> field) {


    public Map<String, String> fieldMap() {
        return FieldDTO.toMap(field);
    }
}
