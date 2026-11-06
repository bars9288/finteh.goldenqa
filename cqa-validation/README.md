# CQA Validation

Реализация общего контракта `FormValidator` из `cqa-api`.
`BeanFormValidator` принимает `jakarta.validation.Validator`, проверяет аннотации
моделей и возвращает отсортированные ошибки с путями полей. Служебный сегмент
`.<list element>` удаляется из сообщений.

Модуль зависит только от `cqa-api` и `cqa-model` внутри проекта. Spring отсутствует.
Провайдер Hibernate Validator предоставляет приложение; bootstrap связывает его с адаптером.

`Validator<T>`, `Rule<T>` и исключения находятся в API. Предметные валидаторы
в business и scenario-engine используют `FormValidator` через конструктор.
Они не зависят от этого модуля и не наследуют его классы.

```bash
mvn -pl cqa-validation -am package
```
