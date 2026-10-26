package ru.cqa.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Какие блоки информации об участнике вернуть в ответе. */
@Schema(description = "Состав ответа")
public record IncludeDTO(

        @Schema(description = "Блок информации о запрашиваемых к ответу сущностях", example = "[\"SOURCE\", \"REQUISITE\", \"ATTRIBUTE\", \"RELATION\"]")
        List<String> partyInfo) {
}
