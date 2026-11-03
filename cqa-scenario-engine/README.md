# CQA Scenario Engine

Архитектурный каркас универсального движка последовательного исполнения сценариев CQA.

`ru.cqa.scenario` · артефакт `cqa-scenario-engine` · [← к карте проекта](../README.md)

## Назначение

Модуль задаёт модели сценариев и контракты для исполнения шагов, проверок, извлечения значений, предварительных условий, подключений, хранения JSON-сценариев и JAR-плагинов. Есть минимальная реализация последовательной стратегии и реестров. Конкретные HTTP, SQL, Kafka и SOAP-операции не реализованы.

## Границы

- ядро не зависит от Spring, JDBC, PostgreSQL-драйверов, Kafka-клиентов и HTTP/SOAP-библиотек;
- конфигурации подключений и секреты поступают через внешние адаптеры CQA;
- интерфейсы `ScenarioRepository`, `ScenarioParser` и `ScenarioSerializer` не содержат PostgreSQL- или JSON-библиотечной реализации;
- встроенные исполнители и внешние JAR-плагины регистрируются через контракты `MoveExecutor` и `ScenarioPlugin`.

## Пакеты

| Пакет | Назначение |
|---|---|
| `definition.model`, `definition.model.move` | сценарии, шаги, зависимости, перемещения и типизированные модели HTTP/SQL/Kafka/SOAP |
| `execution` | жизненный цикл прогона и последовательная стратегия |
| `executor` | контракт исполнителя перемещения и реестр |
| `plugin`, `action` | SPI плагинов, загрузка через `ServiceLoader`, декларации действий |
| `repository`, `serialization` | порты хранения и JSON-парсинга/сериализации |
| `assertion`, `extract` | контракты и диспетчеризация проверок и извлечения |
| `precondition`, `connection`, `definition.validation` | проверки допуска, разрешение подключений и валидация модели |
| `result` | статусы и результаты перемещений, шагов и запусков |

Подробнее: [ARCHITECTURE.md](ARCHITECTURE.md).

## Зависимости

### Внутренние

- `cqa-validation` — общий механизм Bean Validation и базовые контракты для прикладных правил.

### Внешние

- Jakarta Validation API для аннотаций Bean Validation;
- Hibernate Validator как runtime-провайдер.

`ScenarioValidator` принимает `jakarta.validation.Validator` через конструктор. Сам провайдер
Bean Validation подключает приложение, использующее движок (сейчас — `cqa-server`).

## Сборка

```bash
mvn -pl cqa-scenario-engine -am package
```

## Тесты

_Пока нет._
