# CQA Business

Бизнес-логика: доменные объекты, проверки входящих запросов, преобразование DTO в домен.

`ru.cqa.mdm` · артефакт `cqa-business` · [← к карте проекта](../README.md)

> **Статус:** проверки запроса saveAndMerge (физлицо, юрлицо) и приветствия; демо-домен приветствий.
> 27.10.2026 в модуль влит бывший `cqa-case` (проверки запросов).

## Назначение

Всё, что решает «правильный ли запрос и что он значит», без привязки к Spring и транспорту:
прикладные правила и валидаторы запросов, доменные объекты и перевод в них DTO из `cqa-dto`.
Общий механизм валидации находится в `cqa-validation`; сценарии и прогоны выделены
в независимый модуль `cqa-scenario-engine`.

## Границы — что сюда НЕ кладём

- Spring и любой фреймворк — модуль остаётся чистым (см. правило 2 в
  [корневом README](../README.md#архитектурные-правила));
- работу с сетью, БД и брокерами — это дело адаптеров в `cqa-server`;
- логику в DTO: `cqa-dto` — только данные, превращение DTO в домен живёт здесь (`*Mapper`, `FieldMaps`).

## Зависимости

### Внутренние

- `cqa-dto` — DTO входящих запросов: их проверяют правила и переводят в домен мапперы.
- `cqa-validation` — общий базовый валидатор, интерфейс правила и базовое исключение.

### Внешние

Jakarta Validation API приходит через `cqa-dto` и `cqa-validation`. Реализацию провайдера
(Hibernate Validator) подключает `cqa-server`.

## Структура пакетов

```
ru.cqa.mdm
├── greeting                  — демо-домен приветствий
│   ├── GreetingRegistry, GreetingService
│   ├── operator              — Operator, OperatorGreetingService, OperatorMapper
│   └── externalservice       — ExternalService, ExternalServiceGreetingService, ExternalServiceMapper
├── constants                 — имена реквизитов, типы атрибутов, регулярки, PartyType
├── exceptions                — специализированные ошибки валидаторов
├── utils                     — DateFormatUtils, FieldMaps
└── validation                — проверки входящих запросов
    ├── rules                 — реализации Rule; greeting/, party/ (PartyRules + common/, physical/, legal/)
    └── validators            — SaveAndMergeValidator, GreetingValidator

`Validator<D, T, E>`, `Rule<T>` и базовый `ValidationException` расположены в `cqa-validation`.
```

Подробно о проверках saveAndMerge:
[правила бизнес-логики](src/main/java/ru/cqa/cases/mdm/validation/rules/party/README.md) и
[проверки формы DTO](src/main/java/ru/cqa/cases/mdm/validation/rules/party/README_JakartaValid.md).

Как устроена проверка: `Validator<D, T>` из `cqa-validation` реализует метод `validateBody()`: форма (аннотации DTO
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
