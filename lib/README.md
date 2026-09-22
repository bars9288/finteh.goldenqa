# lib — склад jar-зависимостей

Все jar, участвующие в работе приложения, разложены по тематикам.
Содержимое собирается из дерева зависимостей модуля `cqa-server`, вручную сюда
ничего не кладётся.

**Склад — витрина, а не репозиторий.** Сборка его не использует: в pom нет ни
`system`-зависимостей, ни `<repository>`, указывающего сюда. Maven берёт всё из
`~/.m2` и Central. Удаление `lib/` на сборку и запуск не влияет.

Состояние на 2026-09-22: **106 jar, 40 МБ**.

| Папка | jar | Размер | Что внутри |
|-------|----:|-------:|------------|
| `spring-boot`        |  16 |   2.5 МБ | Spring Boot: ядро, автоконфигурация, стартеры |
| `spring-framework`   |   7 |   8.1 МБ | Spring Framework: core, beans, context, web, mvc, aop |
| `web-server`         |   3 |   4.0 МБ | встроенный Tomcat |
| `openapi`            |  11 |   3.2 МБ | springdoc, Swagger UI, Scalar — документация REST |
| `soap`               |   9 |   1.4 МБ | Spring WS, SAAJ, wsdl4j, spring-oxm — SOAP-слой |
| `validation`         |   5 |   1.6 МБ | Bean Validation: Hibernate Validator (пришла со springdoc) |
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

**Валидацию никто не подключал явно.** `hibernate-validator` и компания пришли
транзитивно вместе со `springdoc-openapi-starter-common`. Следствие приятное:
`@Valid` и `@NotBlank` доступны без отдельной зависимости.

**Стартеры — пустые агрегаторы.** `spring-boot-starter-*` не содержат классов и
в собранный fat-jar не попадают, хотя область видимости у них `compile`.

**Свой модуль сюда не кладётся.** `cqa-business` — часть проекта, а не внешняя
зависимость.

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

Важно: `package` и `-am` обязательны. Без них плагин не найдёт `cqa-business` —
модуль не установлен в `~/.m2`, и резолв падает с
`Could not find artifact ru.cqa:cqa-business`.

Дальше содержимое `cqa-server/target/deps` раскладывается по папкам выше.
Разделение runtime и test определяется по составу fat-jar: что попало в
`BOOT-INF/lib` — runtime, остальное (кроме стартеров) — тестовое.

## Работа без интернета

Для **запуска** склад не нужен: fat-jar несёт все зависимости внутри, достаточно
Java 21.

Для **сборки** на изолированной машине склада недостаточно — Maven требует
репозиторий своей структуры, а не папки с jar. Нужен перенос `~/.m2/repository`
или внутренний Nexus.

## Чего в проекте нет

Работы с БД нет — ни драйверов, ни Spring Data, ни ORM. Когда слой БД появится,
здесь заведутся папки `persistence` и `spring-data`.
