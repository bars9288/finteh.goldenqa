# CQA Business

Бизнес-логика: доменные объекты, проверки входящих запросов, преобразование DTO в домен.

`ru.cqa.business` · артефакт `cqa-business` · [← к карте проекта](../README.md)

> **Статус:** проверки запроса saveAndMerge (физлицо, юрлицо) и приветствия; демо-домен приветствий.
> 27.10.2026 в модуль влит бывший `cqa-case` (проверки запросов).

## Назначение

Всё, что решает «правильный ли запрос и что он значит», без привязки к Spring и транспорту:
правила и валидаторы запросов, доменные объекты и перевод в них DTO из `cqa-dto`.
Дальше сюда же лягут сценарии и прогоны из ТЗ (блоки `mainInfo`, `clientsInfo`,
`servicesInfo`, `RulesList`; формат — примерами в `scenarLib/src/example/`).

## Границы — что сюда НЕ кладём

- Spring и любой фреймворк — модуль остаётся чистым (см. правило 2 в
  [корневом README](../README.md#архитектурные-правила));
- работу с сетью, БД и брокерами — это дело адаптеров в `cqa-server`;
- логику в DTO: `cqa-dto` — только данные, превращение DTO в домен живёт здесь (`*Mapper`, `FieldMaps`).

## Зависимости

### Внутренние

- `cqa-dto` — DTO входящих запросов: их проверяют правила и переводят в домен мапперы.

### Внешние

Только `jakarta.validation-api` (приезжает через `cqa-dto`): интерфейс
`Validator`. Движок (hibernate-validator) передаёт `cqa-server`.

## Структура пакетов

```
ru.cqa.business
├── greeting                  — демо-домен приветствий
│   ├── GreetingRegistry, GreetingService
│   ├── operator              — Operator, OperatorGreetingService, OperatorMapper
│   └── externalservice       — ExternalService, ExternalServiceGreetingService, ExternalServiceMapper
├── constants                 — имена реквизитов, типы атрибутов, регулярки, PartyType
├── exceptions                — ValidationException и наследники
├── utils                     — DateFormatUtils, FieldMaps
└── validation                — проверки входящих запросов
    ├── rules                 — Rule; greeting/, party/ (PartyRules + common/, physical/, legal/)
    └── validators            — Validator<D, T>, SaveAndMergeValidator, GreetingValidator
```

Подробно о проверках saveAndMerge:
[правила бизнес-логики](src/main/java/ru/cqa/business/validation/rules/party/README.md) и
[проверки формы DTO](src/main/java/ru/cqa/business/validation/rules/party/README_JakartaValid.md).

Как устроена проверка: `Validator<D, T>` — шаблонный метод `validate()`: форма (аннотации DTO
через `jakarta.validation.Validator`) → сущность для правил (`entity()`) → правила → при ошибках
исключение наследника `ValidationException` (`exception()`).

## Сборка

```bash
mvn -pl cqa-business -am package
```

## Тесты

_Пока нет._

## Открытые вопросы

- [x] Граница между `cqa-case` и `cqa-business` — `cqa-case` влит в `cqa-business`.
- [ ] Версионирование схемы сценария (`scenarioVersion`)
