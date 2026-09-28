# CQA Case

Сценарии и кейсы.

`ru.cqa.cases` · артефакт `cqa-case` · [← к карте проекта](../README.md)

> **Статус:** первый кейс — проверка запроса saveAndMerge.

## Назначение

_TODO: одним абзацем — за что отвечает модуль._

Ориентир: описание сценария (блоки `mainInfo`, `clientsInfo`, `servicesInfo`,
`RulesList`), его разбор и правила перехода между шагами. Формат описан
примерами в `scenarLib/src/example/`.

## Границы — что сюда НЕ кладём

- Spring и любой фреймворк — модуль остаётся чистым (см. правило 2 в
  [корневом README](../README.md#архитектурные-правила));
- работу с сетью, БД и брокерами — это дело адаптеров;
- _TODO: явно развести с `cqa-business`, граница пока размыта._

## Зависимости

### Внутренние

- `cqa-dto` — DTO входящих запросов, которые проверяют кейсы.

### Внешние

Только `jakarta.validation-api` (приезжает через `cqa-dto`): интерфейс
`Validator`. Движок (hibernate-validator) передаёт `cqa-server`.

## Точки расширения

_TODO._ Ориентиры из формата сценария: `anyType` (протокол помимо REST/SOAP),
`typeService` (тип внешнего сервиса), `checkFlags` (правила валидации).

## Структура пакетов

```
ru.cqa.cases
└── saveAndMerge   — SaveAndMergeValidator, SaveAndMergeValidationException
```

## Сборка

```bash
mvn -pl cqa-case package
```

## Тесты

_Пока нет._

## Открытые вопросы

- [ ] Граница между `cqa-case` и `cqa-business`
- [ ] Версионирование схемы сценария (`scenarioVersion`)
- [x] Имя Java-пакета: `case` — зарезервированное слово Java, выбран `ru.cqa.cases`.
