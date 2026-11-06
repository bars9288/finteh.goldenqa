package ru.cqa.api.validation;

import java.util.List;

/** Проверка формы модели. Не содержит предметных правил или типов конкретного провайдера. */
@FunctionalInterface
public interface FormValidator {
    List<String> validate(Object model);
}
