# CQA API

Публичные контракты действий. Единственная зависимость внутри проекта — `cqa-model`.
Spring и реализации сервисов здесь отсутствуют.

| Пакет `ru.cqa.api` | Контракты |
|---|---|
| `scenario` | ScenarioCreator, ScenarioEngine, ScenarioRepository, ScenarioParser, ScenarioSerializer |
| `execution` | MoveExecutor, ExecutionContext |
| `assertion`, `extract` | AssertionEvaluator, Extractor |
| `validation` | Validator<T>, FormValidator, Rule<T>, ValidationException и предметные ошибки |
| `greeting` | GreetingFacade |

Контракт `ExecutionContext` описывает состояние одного запуска; реализация находится
в движке и создаётся заново для каждого выполнения. Исключения находятся в API,
чтобы веб-слой мог обрабатывать ошибки без зависимости от модуля реализации.

`ScenarioAlreadyExistsException` и `ScenarioNotFoundException` расположены в `scenario`.
DTO и модели не дублируются: все сигнатуры используют `ru.cqa.model`.

```bash
mvn -pl cqa-api -am package
```
