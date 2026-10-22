package ru.cqa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

@Schema(description = "Участник")
public record PartyDTO(

        @Schema(description = "Тип участника", example = "PHYSICAL", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Не должно быть пустым")
        String type,

        @Schema(description = "Система-источник", example = "SBL", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Не должно быть пустым")
        String sourceSystem,

        @Schema(description = "Реквизиты участника")
        @NotEmpty(message = "Не должна быь коллекция пустой")
        List<@NotNull @Valid FieldDTO> field,

        @Schema(description = "Уникальный идентификатор строки", example = "123456789abcdef", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Не должно быть пустым")
        String rawId,

        @Schema(description = "Атрибуты участника")
        List<@Valid AttributeDTO> attribute) {


    public Map<String, String> fieldMap() {
        return FieldDTO.toMap(field);
    }
}
