package ru.cqa.model.mdm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Атрибут участника")
public record AttributeDTO(

        @Schema(description = "Тип атрибута", example = "ADDRESS", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "не должно быть пустым")
        String type,

        @Schema(description = "Идентификатор строки", example = "12")
        @NotBlank(message = "не должно быть пустым")
        String rawId,

        @Schema(description = "Поля атрибута")
        List<@NotNull @Valid FieldDTO> field) {
}
