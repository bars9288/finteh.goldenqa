package ru.cqa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

/** Атрибут участника (документ и т.п.) со своим набором полей. */
@Schema(description = "Атрибут участника")
public record AttributeDTO(
        @Schema(description = "Тип атрибута", example = "DOC", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String type,
        @Schema(description = "Идентификатор строки", example = "12")
        String rowId,
        @Schema(description = "Поля атрибута")
        List<@Valid FieldDTO> field) {

    /** Поля атрибута как имя → значение. */
    public Map<String, String> fieldMap() {
        return FieldDTO.toMap(field);
    }
}
