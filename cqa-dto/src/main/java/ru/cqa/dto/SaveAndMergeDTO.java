package ru.cqa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

@Schema(description = "Запрос к MDM saveAndMerge на сохранение (слияние) участника")
public record SaveAndMergeDTO(

        @Schema(description = "Участник запроса", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "Требуется 1 объект")
        @Size(max = 1, message = "Требуется 1 объект")
        List<@NotNull @Valid PartyDTO> party,

        @Schema(description = "Запрашиваемых к ответу блоки данных участника", example = "[\"SOURCE\", \"REQUISITE\", \"ATTRIBUTE\", \"RELATION\"]")
        @Valid
        IncludeDTO include,

        @Schema(description = "Тип обновления", example = "FULL", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Не должно быть пустым")
        String updateType) {
}
