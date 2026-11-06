# CQA Scenario Engine

Создание, проверка и последовательное исполнение сценариев. Зависит только
от `cqa-api` и `cqa-model` внутри проекта, не использует Spring и сетевые клиенты.

`ScenarioCreationService` реализует `ScenarioCreator`; `DefaultScenarioEngine` —
`ScenarioEngine`. Парсинг, хранилище, проверка формы и исполнители поступают
через интерфейсы. Модели сценариев и результатов находятся в `cqa-model`.

Внутри движка остаются стратегии, runners, реестры, диспетчеризация assertions/extracts,
JSONPath-чтение и `DefaultExecutionContext`. Предметный `ScenarioValidator`
делегирует проверку формы `FormValidator` и выдаёт ошибку из API.

HTTP-адаптер находится в `cqa-server`, сборка компонентов — в `cqa-bootstrap`.
SQL/Kafka/SOAP-исполнители и подсистема плагинов пока не подключены.

Подробнее: [архитектура](ARCHITECTURE.md), [пошаговый запуск](../MANUAL.md).

```bash
mvn -pl cqa-scenario-engine -am package
```
