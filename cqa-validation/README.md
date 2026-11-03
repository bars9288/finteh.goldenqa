# CQA Validation

Общий механизм проверки моделей, используемый бизнес-логикой и движком сценариев.

`ru.cqa.validation` · артефакт `cqa-validation` · [← к карте проекта](../README.md)

## Состав

- `Validator<D, T, E>` — базовый поток: Bean Validation входной модели, затем прикладные правила сущности;
- `Rule<T>` — контракт прикладного правила;
- `ValidationException` — базовая ошибка со списком сообщений.

Модуль не зависит от `cqa-business`, `cqa-scenario-engine` или Spring. Конкретные валидаторы,
правила и классы ошибок остаются в своих предметных модулях. Приложение подключает реализацию
Jakarta Bean Validation provider; этот модуль содержит только API.

## Зависимости

- `jakarta.validation-api`.

## Сборка

```bash
mvn -pl cqa-validation -am package
```
