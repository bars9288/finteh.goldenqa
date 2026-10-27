# CQA DTO

Транспортные контракты: тела запросов и ответов REST и SOAP — только данные и аннотации.

`ru.cqa.dto` · артефакт `cqa-dto` · [← к карте проекта](../README.md)

## Назначение

Описывает, что приходит и уходит «по проводу»: структура JSON и XML, ограничения формы
(Bean Validation), описание для Swagger, разметка JAXB для SOAP. Ни логики, ни преобразований —
в домен DTO переводит `cqa-businesslogic` (`OperatorMapper`, `ExternalServiceMapper`, `FieldMaps`).

## Границы — что сюда НЕ кладём

- **методы** — ни `toXxx()`, ни вспомогательных; у SOAP-DTO допустимы только геттеры и сеттеры для JAXB;
- зависимости на свои модули — `cqa-dto` не знает ни о домене, ни о сервере;
- Spring, парсеры и движки — здесь только API аннотаций; реализации подключает `cqa-server`.

## Соглашения

- имя класса оканчивается на `DTO`: `OperatorDTO`, не `OperatorDto`;
- REST — record без пометки транспорта (`OperatorDTO`); SOAP — обычный класс `{Предмет}SoapDTO`
  в пакете `soap` (JAXB не работает с record);
- пакеты плоские: всё в `ru.cqa.dto`, отдельный подпакет только для транспорта (`soap`);
- у каждой аннотации проверки свой `message` — иначе текст зависит от локали JVM.

## Зависимости

### Внутренние

Нет.

### Внешние

Только API аннотаций, версии заданы явно (BOM Spring сюда не импортируется):
`jackson-annotations`, `jakarta.validation-api`, `swagger-annotations-jakarta`, `jakarta.xml.bind-api`.

## Состав

```
ru.cqa.dto
├── SaveAndMergeDTO, PartyDTO, FieldDTO, AttributeDTO, IncludeDTO   запрос saveAndMerge
├── OperatorDTO, ExternalServiceDTO                                 запросы приветствий (REST)
├── HelloResponseDTO                                                ответ приветствий
└── soap
    ├── OperatorSoapDTO, ExternalServiceSoapDTO                     запросы приветствий (SOAP)
    └── SoapNamespaces, package-info                                пространства имён
```

Ограничения формы для saveAndMerge с примерами —
[README_JakartaValid](../cqa-businesslogic/src/main/java/ru/cqa/businesslogic/validation/rules/party/README_JakartaValid.md).

## Сборка

```bash
mvn -pl cqa-dto package
```
