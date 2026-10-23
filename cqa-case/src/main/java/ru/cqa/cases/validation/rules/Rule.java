package ru.cqa.cases.validation.rules;

import java.util.List;

@FunctionalInterface
public interface Rule<T> {

    List<String> check(T requestDTO);

    default boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
