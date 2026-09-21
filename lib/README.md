# lib — склад jar-зависимостей

Все jar, участвующие в работе приложения, разложены по тематикам.
Содержимое собирается из дерева зависимостей модулей, вручную сюда ничего не кладётся.

| Папка              | Что внутри                                                        |
|--------------------|-------------------------------------------------------------------|
| `spring-boot`      | Spring Boot: стартеры, автоконфигурация, ядро                      |
| `spring-framework` | Spring Framework: core, beans, context, web, mvc                   |
| `web-server`       | встроенный Tomcat                                                  |
| `json`             | Jackson, json-path, json-smart                                     |
| `xml-bind`         | активация и Jakarta XML bind                                       |
| `logging`          | SLF4J, Logback, мосты Log4j и JUL                                  |
| `metrics`          | Micrometer                                                         |
| `config`           | SnakeYAML                                                          |
| `jakarta-api`      | аннотации Jakarta                                                  |
| `testing`          | JUnit, Mockito, AssertJ, spring-test — только для тестов           |
| `misc`             | служебное: аннотации nullability                                   |

Работы с БД в проекте пока нет — ни драйверов, ни Spring Data, ни ORM.
Когда слой БД вернётся, здесь появятся папки `persistence` и `spring-data`.

## Обновление

```bash
export JAVA_HOME=/Users/sergey/Library/Java/JavaVirtualMachines/ms-21.0.11/Contents/Home
mvn -pl cqa-server dependency:copy-dependencies \
    -DoutputDirectory=target/deps -DincludeScope=test
```

Дальше разложить полученное по папкам выше. Папки повторяют состав дерева
зависимостей — при добавлении модуля или зависимости склад нужно пересобрать.
