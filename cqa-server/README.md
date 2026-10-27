# CQA Server

Spring Boot приложение: REST- и SOAP-ручки, журнал запросов, прокси в МДМ.

`ru.cqa.server` · артефакт `cqa-server` · [← к карте проекта](../README.md)

## Назначение

Точка входа приложения и единственный модуль, который знает про Spring:
поднимает контекст, собирает в бины классы `cqa-businesslogic` (валидаторы, наборы правил,
реестр приветствий), принимает запросы по REST и SOAP, пишет их в журнал
и пересылает проверенный saveAndMerge в МДМ.

## Границы — что сюда НЕ кладём

- доменные объекты, правила и валидаторы — они живут в `cqa-businesslogic`;
- DTO — они в `cqa-dto`;
- логику, которую нельзя проверить без поднятия контекста Spring.

## Зависимости

### Внутренние

- `cqa-businesslogic` — валидаторы, правила, домен приветствий, мапперы DTO → домен;
- `cqa-dto` — тела запросов REST и SOAP.

### Внешние

| Зависимость                                 | Назначение                                  |
|---------------------------------------------|---------------------------------------------|
| `spring-boot-starter-web`                   | Spring MVC на встроенном Tomcat             |
| `spring-boot-starter-web-services`, `wsdl4j`| SOAP: Spring WS, WSDL из XSD на лету        |
| `spring-boot-starter-validation`            | Hibernate Validator для аннотаций DTO       |
| `spring-boot-starter-data-jdbc`, `sqlite-jdbc` | журнал запросов в SQLite                 |
| `springdoc-openapi-starter-webmvc-ui` / `-scalar` | Swagger UI и Scalar                   |
| `spring-boot-starter-test`                  | тесты (scope `test`)                        |

BOM `spring-boot-dependencies` **4.1.1** импортируется в этом модуле, а не в
корне, — поэтому остальные модули остаются свободными от Spring.

## Ручки

Адреса задаются в `connections.yml` (`cqa.api.paths.*`) и переопределяются переменными окружения или `.env`.

| Адрес по умолчанию   | Переменная                    | Метод    | Что делает                                                  |
|----------------------|-------------------------------|----------|-------------------------------------------------------------|
| `/mdm/saveAndMerge`  | `CQA_PATH_SAVE_AND_MERGE`     | POST     | проверка запроса и прозрачная пересылка в МДМ               |
| `/hello`             | `CQA_PATH_GREETING_OPERATOR`  | GET/POST | приветствие оператору, проверка имени и фамилии             |
| `/helloService`      | `CQA_PATH_GREETING_SERVICE`   | GET/POST | приветствие внешнему сервису                                |
| `/echo`              | `CQA_PATH_ECHO`               | POST     | эхо: вернуть полученный запрос (`/echo/details` — с разбором) |
| `/services`          | `CQA_PATH_SOAP`               | POST     | все SOAP-операции; WSDL — `/services/greeting.wsdl`, `/services/echo.wsdl` |

Документация API: `/swagger-ui.html`, `/scalar`, `/v3/api-docs`.

### saveAndMerge по шагам

1. `JournalFilter` пишет запрос в журнал (`journal_request`).
2. Контроллер разбирает тело в `SaveAndMergeDTO` (тело не JSON — 400 от `ApiExceptionHandler`).
3. По `party[0].type` выбирается валидатор: `PHYSICAL` или `LEGAL`; другой тип — 400.
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

## Структура пакетов

```
ru.cqa.server
├── CqaServerApplication, StartupBanner   точка входа и баннер в лог
├── config        бины: валидаторы и правила, приветствия, клиент МДМ, SOAP, OpenAPI, журнал
├── web           ApiPaths, ApiExceptionHandler, ErrorResponse
│   ├── saveAndMerge   контроллер + обработчик ошибок
│   ├── greeting       контроллеры приветствий + обработчик ошибок
│   ├── echo           эхо-ручки
│   └── journal        JournalFilter, CachedBodyRequest — журнал входящих REST-запросов
├── ws            SOAP-эндпоинты: greeting, echo
├── mdm           MdmClient — прозрачный прокси в МДМ
└── db/sqlite     диалект SQLite; journal — таблицы, репозитории, JournalWriter, резервный файл
```

## Журнал

SQLite, файл задаётся `CQA_DB_URL` (по умолчанию `cqa-journal.db` в рабочем каталоге).
Таблицы создаются при старте из `src/main/resources/db/schema.sql`:
`journal_request` — входящий запрос и ответ клиенту, `journal_call` — вызовы МДМ.
Если БД недоступна, приложение работает дальше, а журнал пишется в резервный файл
`logs/<дата>-journal.log`.

## Сборка и запуск

```bash
mvn -pl cqa-server -am package
java -jar cqa-server/target/cqa-server-0.0.4.jar   # из корня репозитория
```

`spring-boot-maven-plugin` выполняет `repackage` и `build-info` (версия для баннера и Swagger);
на выходе — исполняемый fat-jar со `Start-Class: ru.cqa.server.CqaServerApplication`.

## Конфигурация

`application.yml`, `connections.yml` и `.env` лежат в корне репозитория; запускать из корня.
Переменные — в [корневом README](../README.md#конфигурация) и `.env.example`.

## Тесты

Удалены 26.10.2026, будут написаны заново. В `src/test` лежит только `probe/MdmProbe` —
ручная проверка связи с МДМ через `main` (берёт `MDM_DEV_*` из окружения или `.env`).

## Открытые вопросы

- [ ] Тесты: юнит на правила и валидаторы, интеграционные на ручки
- [ ] SOAP: журналирование `/services`, код Fault по умолчанию (`Client` на любую ошибку)
- [ ] Пакет `db/sqlite/journal` → `db/journal` перед переходом на другую БД
