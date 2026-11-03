package ru.cqa.validation;

import java.util.List;

/** Дополнительная проверка сущности, которую нельзя выразить Bean Validation-аннотациями. */
@FunctionalInterface
public interface Rule<T> {

    List<String> check(T entity);

    default boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
