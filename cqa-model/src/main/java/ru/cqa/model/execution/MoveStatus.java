package ru.cqa.model.execution;

/** Итог выполнения одной операции. */
public enum MoveStatus {
    SUCCESS,
    FAILED,
    ERROR,
    TIMEOUT,
    SKIPPED
}
