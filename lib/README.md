# lib — склад jar-зависимостей

Все jar, участвующие в работе приложения, разложены по тематикам.
Содержимое собирается из дерева зависимостей модуля `cqa-server`, вручную сюда
ничего не кладётся.

**Склад — витрина, а не репозиторий.** Сборка его не использует: в pom нет ни
`system`-зависимостей, ни `<repository>`, указывающего сюда. Maven берёт всё из
`~/.m2` и Central. Удаление `lib/` на сборку и запуск не влияет.

Состояние на 2026-10-27: **122 jar, 55 МБ** (состав не менялся с 2026-10-12).

| Папка | jar | Размер | Что внутри |
|-------|----:|-------:|------------|
| `spring-boot`        |  25 |   2.8 МБ | Spring Boot: ядро, автоконфигурация, стартеры |
| `spring-framework`   |   9 |   8.8 МБ | Spring Framework: core, beans, context, web, mvc, aop, jdbc, tx |
| `web-server`         |   3 |   4.0 МБ | встроенный Tomcat |
| `spring-data`        |   3 |   2.8 МБ | Spring Data JDBC: commons, relational, jdbc — журнал запросов |
| `persistence`        |   2 |  11.6 МБ | драйвер SQLite и пул соединений HikariCP |
| `openapi`            |  11 |   3.2 МБ | springdoc, Swagger UI, Scalar — документация REST |
| `soap`               |   9 |   1.4 МБ | Spring WS, SAAJ, wsdl4j, spring-oxm — SOAP-слой |
| `validation`         |   5 |   1.6 МБ | Bean Validation: Hibernate Validator |
| `json`               |   7 |   4.9 МБ | Jackson 2.x и 3.x одновременно — см. примечание ниже |
| `xml-bind`           |   8 |   1.4 МБ | JAXB и активация: разбор XML для SOAP |
| `logging`            |   7 |   1.4 МБ | SLF4J, Logback, мосты Log4j и JUL |
| `metrics`            |   2 |   0.1 МБ | Micrometer |
| `config`             |   1 |   0.3 МБ | SnakeYAML — разбор application.yml |
| `jakarta-api`        |   1 |   0.0 МБ | аннотации Jakarta |
| `testing`            |  27 |  10.3 МБ | JUnit, Mockito, AssertJ, spring-test — только область test |
| `misc`               |   2 |   0.7 МБ | служебное: аннотации nullability, commons-lang3 |

## Примечания

**Два Jackson сразу.** Spring Boot 4 перешёл на Jackson 3 (пакет `tools.jackson`),
а `swagger-core-jakarta` из springdoc остался на Jackson 2 (`com.fasterxml.jackson`).
Пакеты разные, конфликта нет, но в `json/` лежат обе линейки.

**Валидация подключена явно.** `cqa-server` объявляет `spring-boot-starter-validation`
(движок Hibernate Validator), а `cqa-dto` — только `jakarta.validation-api` для
аннотаций. Те же jar приходят и транзитивно со `springdoc-openapi-starter-common`,
поэтому состав папки `validation/` от этого не изменился — добавился лишь пустой
стартер в `spring-boot/`.

**Стартеры — пустые агрегаторы.** `spring-boot-starter-*` не содержат классов и
в собранный fat-jar не попадают, хотя область видимости у них `compile`.

**Свои модули сюда не кладутся.** `cqa-dto`, `cqa-business` — часть
проекта, а не внешние зависимости.

**`spring-boot-jarmode-tools` здесь нет намеренно.** Его добавляет в fat-jar сам
`spring-boot-maven-plugin` при `repackage`; зависимостью проекта он не является
и для компиляции в IDE не нужен.

**Все jar собраны под Java 17 или ниже** (class-file ≤ 61). Стенд работает на Java 21,
поэтому допустимо до class-file 65. После пересборки склада проверку стоит повторить.

## Пересборка склада

При любом изменении зависимостей склад нужно пересобрать, иначе он разойдётся
с реальностью.

```bash
export JAVA_HOME=/Users/sergey/Library/Java/JavaVirtualMachines/ms-21.0.11/Contents/Home

mvn -o clean package dependency:copy-dependencies \
    -DskipTests -DincludeScope=test \
    -DoutputDirectory=target/deps \
    -pl cqa-server -am
```

Важно: `package` и `-am` обязательны. Без них плагин не найдёт свои модули
(`cqa-dto`, `cqa-business`) — они не установлены в `~/.m2`, и резолв
падает с `Could not find artifact ru.cqa:...`.

Дальше содержимое `cqa-server/target/deps` (кроме `cqa-*.jar`) раскладывается
по папкам выше.
Разделение runtime и test определяется по составу fat-jar: что попало в
`BOOT-INF/lib` — runtime, остальное (кроме стартеров) — тестовое.

## Работа без интернета

Для **запуска** склад не нужен: fat-jar несёт все зависимости внутри, достаточно
Java 21.

Для **сборки** на изолированной машине склада недостаточно — Maven требует
репозиторий своей структуры, а не папки с jar. Нужен перенос `~/.m2/repository`
или внутренний Nexus.

## Работа с БД

Журнал запросов хранится в SQLite через Spring Data JDBC (без ORM, без Hibernate).
Сервер БД не нужен: база — один файл в рабочем каталоге.

**Драйвер SQLite — самый тяжёлый jar склада (~11 МБ).** Внутри лежат нативные
библиотеки SQLite под все платформы (Windows, Linux, macOS), драйвер сам выбирает
нужную при запуске. Отдельно ставить SQLite на стенд не требуется.

**Диалекта SQLite в Spring Data JDBC нет** — он описан в проекте
(`ru.cqa.server.db.sqlite.SqliteDialect`), дополнительных jar для этого не нужно.
