package ru.cqa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Тело POST-запроса saveAndMerge.
 *
 * <p>Пример:
 * <pre>{@code
 * {
 *   "party": [
 *     {
 *       "type": "ФИЗИЧЕСКОЕЛИЦО",
 *       "sourseSystem": "ВНЕШНЯЯ СИСТЕМА",
 *       "field": [
 *         {"name": "name", "value": "Иван"},
 *         {"name": "surname", "value": "Иванов"}
 *       ],
 *       "attribute": [
 *         {"type": "ДОКУМЕНТ", "rowId": "12456789", "field": [{"name": "pasportNumber", "value": "123456"}]}
 *       ]
 *     }
 *   ],
 *   "include": {"partyInfo": ["МАМА", "МЫЛА", "РАМУ", "ОКНА"]},
 *   "updateType": "FULL"
 * }
 * }</pre>
 */
@Schema(description = "Запрос на сохранение и слияние участников")
public record SaveAndMergeDTO(
        @Schema(description = "Участники", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty List<@Valid PartyDTO> party,
        @Schema(description = "Какие блоки вернуть в ответе")
        @Valid IncludeDTO include,
        @Schema(description = "Тип обновления", example = "FULL", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String updateType) {
}
