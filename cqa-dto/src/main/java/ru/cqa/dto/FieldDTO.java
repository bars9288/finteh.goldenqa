package ru.cqa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Schema(description = "Поле: имя и значение")
public record FieldDTO(

        @Schema(description = "Имя поля", example = "surname", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Не должно быть пустым")
        String name,

        @Schema(description = "Значение поля", example = "Иванов")
        String value) {


    static Map<String, String> toMap(List<FieldDTO> fields) {
        Map<String, String> map = new LinkedHashMap<>();
        if (fields != null) {
            for (FieldDTO f : fields) {
                if (f != null) {
                    map.put(f.name(), f.value());
                }
            }
        }
        return map;
    }
}
