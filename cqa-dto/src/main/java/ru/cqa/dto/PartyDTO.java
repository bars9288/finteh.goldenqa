package ru.cqa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

/**
 * Участник в запросе saveAndMerge: реквизиты ({@code field}) и атрибуты
 * ({@code attribute}).
 *
 * <p>Имя {@code sourseSystem} повторяет написание из контракта — Jackson
 * сопоставляет поля по имени буква в букву.
 */
@Schema(description = "Участник")
public record PartyDTO(
        @Schema(description = "Тип участника", example = "ФИЗИЧЕСКОЕЛИЦО", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String type,
        @Schema(description = "Система-источник", example = "ВНЕШНЯЯСИСТЕМА", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String sourseSystem,
        @Schema(description = "Реквизиты участника")
        List<@Valid FieldDTO> field,
        @Schema(description = "Атрибуты участника")
        List<@Valid AttributeDTO> attribute) {

    /** Реквизиты как имя → значение. */
    public Map<String, String> fieldMap() {
        return FieldDTO.toMap(field);
    }
}
