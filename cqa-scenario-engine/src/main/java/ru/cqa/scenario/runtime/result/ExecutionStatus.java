package ru.cqa.scenario.runtime.result;

/** Итоговый статус жизненного цикла сценария. */
public enum ExecutionStatus {
    CREATED,
    VALIDATING,
    PREPARING,
    RUNNING,
    COMPLETED,
    FAILED,
    ERROR,
    CANCELLED
}
