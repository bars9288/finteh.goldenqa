package ru.cqa.scenario.assertion;

import java.util.Objects;

/** Диагностика вычисления одного Assertion. */
public record AssertionResult(String assertionId, AssertionStatus status, String message) {

    public AssertionResult {
        Objects.requireNonNull(assertionId, "assertionId");
        Objects.requireNonNull(status, "status");
        message = message == null ? "" : message;
    }
}
