# CQA Server

Библиотека веб-слоя и инфраструктурных адаптеров: REST/SOAP, HTTP-клиенты,
JSON-парсинг, файловое хранилище сценариев и SQLite-журнал.
Внутренние зависимости — только `cqa-api` и `cqa-model`.

Контроллеры используют интерфейсы API. `FileScenarioRepository`,
`JacksonScenarioParser`, `JacksonScenarioSerializer` и `HttpMoveExecutor`
реализуют порты API. Связывание этих адаптеров с движком и бизнес-логикой
выполняет `cqa-bootstrap`; собственные SOAP/OpenAPI/JDBC-настройки остаются здесь.

Spring Boot BOM импортируется в server и bootstrap. Здесь нет main-класса
и упаковки исполняемого JAR. `ServerConfiguration` подключает компоненты
пакета `ru.cqa.server` и JDBC-репозитории журнала.

## Ручки

Адреса задаются в `connections.yml` (`cqa.api.paths.*`) и переопределяются переменными окружения или `.env`.

| Адрес по умолчанию   | Переменная                    | Метод    | Что делает                                                  |
|----------------------|-------------------------------|----------|-------------------------------------------------------------|
| `/scenarios` | `CQA_PATH_SCENARIOS` | POST | создать и сохранить сценарий |
| `/scenarios/{id}/executions` | — | POST | выполнить сохранённую версию сценария |
| `/mdm/saveAndMerge`  | `CQA_PATH_SAVE_AND_MERGE`     | POST     | проверка запроса и прозрачная пересылка в МДМ               |
| `/hello`             | `CQA_PATH_GREETING_OPERATOR`  | GET/POST | приветствие оператору, проверка имени и фамилии             |
| `/helloService`      | `CQA_PATH_GREETING_SERVICE`   | GET/POST | приветствие внешнему сервису                                |
| `/echo`              | `CQA_PATH_ECHO`               | POST     | эхо: вернуть полученный запрос (`/echo/details` — с разбором) |
| `/services`          | `CQA_PATH_SOAP`               | POST     | все SOAP-операции; WSDL — `/services/greeting.wsdl`, `/services/echo.wsdl` |

Документация API: `/swagger-ui.html`, `/scalar`, `/v3/api-docs`.

### saveAndMerge по шагам

1. `JournalFilter` пишет запрос в журнал (`journal_request`).
2. Контроллер разбирает тело в `SaveAndMergeDTO` (тело не JSON — 400 от `ApiExceptionHandler`).
3. Контроллер вызывает `Validator<SaveAndMergeDTO>`; бизнес-реализация по `party[0].type` выбирает правила: `PHYSICAL` или `LEGAL`; другой тип — 400.
4. Валидатор проверяет форму (аннотации DTO) и правила; ошибки — 400 одним списком.
5. Проверенный запрос байт в байт уходит в МДМ (`MdmClient`, вызов пишется в `journal_call`),
   ответ МДМ возвращается клиенту как есть.

### Обработка ошибок

| Класс                          | Действует на                 | Порядок  | Что ловит |
|--------------------------------|------------------------------|----------|-----------|
| `SaveAndMergeExceptionHandler` | `SaveAndMergeController`     | первым   | ошибки проверки → 400, МДМ недоступна → 502, МДМ не настроена → 500 |
| `GreetingExceptionHandler`     | контроллеры приветствий      | первым   | ошибки проверки → 400 |
| `ApiExceptionHandler`          | все REST-контроллеры         | последним| не JSON, нет параметра, `@Valid`, `IllegalArgumentException` → 400 (ProblemDetail) |

Ошибки проверки и МДМ приходят телом `ErrorResponse` (`message` + `errors`).
SOAP-ошибки обрабатывает свой `SoapFaultMappingExceptionResolver` → SOAP Fault.

## Журнал

SQLite, файл задаётся `CQA_DB_URL` (по умолчанию `cqa-journal.db` в рабочем каталоге).
Таблицы создаются при старте из `src/main/resources/db/schema.sql`:
`journal_request` — входящий запрос и ответ клиенту, `journal_call` — вызовы МДМ.
Если БД недоступна, приложение работает дальше, а журнал пишется в резервный файл
`logs/<дата>-journal.log`.

## Сборка и запуск

Библиотеку можно собрать отдельно: `mvn -pl cqa-server -am package`.
Для запуска полного приложения из корня репозитория:

```bash
mvn clean verify
java -jar cqa-bootstrap/target/cqa-bootstrap-0.0.4.jar
```

Конфигурация и команды тестирования — в [корневом README](../README.md).
`src/test/.../probe/MdmProbe` остаётся ручной проверкой внешней МДМ и автоматически не запускается.
