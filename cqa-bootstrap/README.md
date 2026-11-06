# CQA Bootstrap

Модуль запуска и сборки приложения. Единственное место, где вместе выбираются
реализации из `server`, `business`, `validation` и `scenario-engine`.

`ru.cqa.bootstrap.CqaApplication` запускает Spring и импортирует
`ServerConfiguration`: серверные компоненты и JDBC-репозитории подключаются явно.
Конфигурации в `ru.cqa.bootstrap.config` собирают сценарный движок,
валидаторы и сервисы приветствий. Настройки инфраструктуры остаются в `cqa-server`.

Spring Boot Maven Plugin выполняет `build-info` и `repackage` только здесь.
Все модули работают в одном процессе, с одним Spring-контекстом и одним исполняемым JAR.

Из корня репозитория:

```bash
mvn clean verify
java -jar cqa-bootstrap/target/cqa-bootstrap-0.0.4.jar
python3 scripts/smoke_scenarios.py
```

Рабочий каталог при обычном запуске — корень проекта: оттуда читаются внешние
`application.yml`, `connections.yml` и `.env`. Smoke-проверка использует собственный
временный каталог и конфигурацию.

В `src/test` проверяются границы модулей, объединение ошибок формы и правил,
сортировка вложенных ошибок, выбор правил ФЛ/ЮЛ и соответствие SOAP-ответов XSD. Веб-проверки собранного JAR
выполняет `scripts/smoke_scenarios.py`.
