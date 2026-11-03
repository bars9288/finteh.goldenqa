package ru.cqa.scenario.definition.model;

import java.util.Objects;
import jakarta.validation.constraints.NotBlank;

/** Правило извлечения значения из результата операции в переменную прогона. */
public record Extract(@NotBlank String id, @NotBlank String type, @NotBlank String sourceExpression,
                      @NotBlank String targetVariable) {
}
