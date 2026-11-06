package ru.cqa.model.scenario;

import jakarta.validation.constraints.NotBlank;

/** Правило извлечения значения из результата операции в переменную прогона. */
public record Extract(@NotBlank String id, @NotBlank String type, @NotBlank String sourceExpression,
                      @NotBlank String targetVariable) {
}
