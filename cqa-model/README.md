# CQA Model

Общие модели и DTO. Модуль заменяет `cqa-dto` и объединяет данные публичных контрактов.
Зависимостей на другие модули проекта и Spring нет.

| Пакет `ru.cqa.model` | Данные |
|---|---|
| `mdm` | SaveAndMergeDTO, PartyDTO, FieldDTO, AttributeDTO, IncludeDTO |
| `greeting`, `greeting.soap` | REST/SOAP-запросы, ответы и XML-пространства имён |
| `echo`, `echo.soap`, `web` | Ответы эха, SOAP-контракты, ErrorResponse |
| `scenario`, `scenario.move` | Scenario, Step, Move, HTTP/SQL/Kafka/SOAP-модели, ответ создания |
| `execution` | Запрос и параметры запуска, результаты, статусы, HttpResponseData |
| `assertion` | Результат и статус проверки |

Здесь находятся структуры данных, ограничения формы и аннотации сериализации.
Исполнение, парсинг, предметные проверки и изменяемый контекст запуска находятся в реализациях.
Внутренние доменные объекты business и записи SQLite-журнала остаются в своих модулях.

Внешние зависимости: API аннотаций Jackson, Jakarta Validation/JAXB и OpenAPI.
JSON/XML-поля публичных запросов и ответов при переносе сохранены; Java-пакеты изменились.

```bash
mvn -pl cqa-model -am package
```
