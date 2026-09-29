package ru.cqa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Пара имя–значение: общий формат реквизитов и полей атрибутов. */
@Schema(description = "Поле: имя и значение")
public record FieldDTO(

        @Schema(description = "Имя поля", example = "surname", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String name,

        @Schema(description = "Значение поля", example = "Иванов")
        String value) {

    /**
     * Список полей как имя → значение в порядке запроса. При повторе имени
     * остаётся последнее значение; {@code null} даёт пустую мапу.
     */
    static Map<String, String> toMap(List<FieldDTO> fields) {
        Map<String, String> map = new LinkedHashMap<>();
        if (fields != null) {
            fields.forEach(f -> map.put(f.name(), f.value()));
        }
        return map;
    }
}
