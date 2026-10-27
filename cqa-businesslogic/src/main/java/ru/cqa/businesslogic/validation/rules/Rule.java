package ru.cqa.businesslogic.validation.rules;

import java.util.List;

@FunctionalInterface
public interface Rule<T> {

    List<String> check(T entityDTO);

    default boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
