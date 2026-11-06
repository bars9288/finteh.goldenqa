package ru.cqa.api.validation;

/** Проверяет входную модель; нарушения возвращаются через ValidationException. */
@FunctionalInterface
public interface Validator<T> {
    void validateBody(T request);
}
