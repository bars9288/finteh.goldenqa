# CQA Scenario Engine: как запускается сценарий

Документ описывает фактический путь в текущей реализации проекта: от старта Spring-приложения до ответа `ExecutionResult` на ручке запуска. Это описание текущего MVP, а не целевая архитектура всей платформы.

## 1. Общая схема

```mermaid
flowchart TD
    A[Старт приложения] --> B[Spring сканирует компоненты и конфигурации]
    B --> C[Создаются ScenarioRepository и HTTP-клиент]
    B --> D[ScenarioEngineConfiguration связывает runtime-компоненты]
    D --> E[ScenarioEngine готов принимать запуски]

    F[POST /scenarios/{id}/executions] --> G[ScenarioExecutionController]
    G --> H[ExecutionParameters]
    H --> I[DefaultScenarioEngine]
    I --> J[FileScenarioRepository читает JSON]
    J --> K[JacksonScenarioParser строит Scenario]
    K --> L[ScenarioValidator]
    L --> M[ExecutionContext]
    M --> N[SequentialExecutionStrategy]
    N --> O[DefaultStepRunner]
    O --> P[DefaultMoveRunner]
    P --> Q[HttpMoveExecutor]
    Q --> R[HTTP-система возвращает ответ]
    R --> S[Assertions и Extracts]
    S --> T[StepResult и ExecutionResult]
    T --> U[JSON-ответ HTTP-ручки]
```

## 2. Этап первый: старт приложения

### 2.1. Точка входа

Файл [`CqaServerApplication.java`](cqa-server/src/main/java/ru/cqa/server/CqaServerApplication.java) содержит `main`:

1. `SpringApplication.run(...)` поднимает Spring-контекст.
2. `@SpringBootApplication` включает автоконфигурацию Spring Boot и сканирование компонентов от пакета `ru.cqa.server` вниз.
3. Когда контекст готов, код получает версию сборки и печатает стартовый баннер.

Во время запуска приложение также поднимает встроенный Tomcat. Порт по умолчанию задан как `8990` в корневом `application.yml`; его можно переопределить через `SERVER_PORT`.

### 2.2. Настройки

В корневом `application.yml` подключаются настройки приложения и опциональные файлы `.env` и `connections.yml`. Там задаются:

- каталог сценариев: `CQA_SCENARIOS_DIR`, по умолчанию `scenarios`;
- порт: `SERVER_PORT`, по умолчанию `8990`;
- путь ручек в `connections.yml`, например `CQA_PATH_SCENARIOS`, по умолчанию `/scenarios`.

`FileScenarioRepository` превращает каталог в абсолютный путь при создании бина. Само содержимое сценария на старте не читает: файл будет прочитан, когда поступит команда запуска.

### 2.3. Репозиторий и JSON-адаптеры

Spring находит `FileScenarioRepository`, `JacksonScenarioParser` и `JacksonScenarioSerializer` по `@Repository`/`@Component`.

- `FileScenarioRepository` реализует интерфейс `ScenarioRepository` и хранит JSON-файлы на диске.
- `JacksonScenarioParser` переводит JSON в объектную модель `Scenario`.
- `JacksonScenarioSerializer` переводит `Scenario` обратно в JSON при сохранении.

При создании сценария `ScenarioController` передаёт JSON в `ScenarioCreationService`: тот разбирает JSON, валидирует объект и сохраняет его. Для версии `1.0` имя сохранённого файла кодирует версию в Base64 URL-safe виде, поэтому в репозитории можно встретить суффикс `MS4w`.

### 2.4. Создание общего HTTP-клиента

`ExternalServicesConfiguration.sharedHttpClient()` создаёт один бин `java.net.http.HttpClient`. Это транспортный клиент приложения с пулом соединений и connect timeout из настроек выбранного scope.

`HttpMoveExecutor` получает этот общий `HttpClient` через конструктор. Во время конкретного Move он дополнительно собирает `JdkClientHttpRequestFactory` и `RestClient`, но транспортный `HttpClient` остаётся общим.

`MdmClient` тоже использует общий `HttpClient`, но его бин помечен `@Lazy`: он создаётся при первом обращении к нему. Сценарный `HttpMoveExecutor` обращается к адресу из `HttpMove.path`; это не вызов `MdmClient`.

### 2.5. Сборка сценарного runtime

[`ScenarioEngineConfiguration.java`](cqa-server/src/main/java/ru/cqa/server/scenario/ScenarioEngineConfiguration.java) связывает компоненты в цепочку:

| Бин | Для чего нужен |
|---|---|
| `ScenarioValidator` | Запускает Jakarta Bean Validation для модели сценария. |
| `MoveExecutorRegistry` | Регистрирует доступные исполнения Move. Сейчас зарегистрирован `HttpMoveExecutor`. |
| `MoveRunner` | Ищет executor по типу Move и проверяет тип результата. |
| `AssertionEngine` | Регистрирует evaluators типов `equals` и `notNull`. |
| `ExtractEngine` | Регистрирует extractor типа `jsonpath`. |
| `StepRunner` | Вызывает Move, затем выполняет его Assertions и Extracts. |
| `ExecutionStrategy` | Использует `SequentialExecutionStrategy`, исполняющую шаги по порядку. |
| `ScenarioEngine` | Реализован как `DefaultScenarioEngine`; загружает и валидирует сценарий, создаёт контекст и запускает стратегию. |

В результате старта приложения ручка уже получает готовый `ScenarioEngine` через конструктор контроллера. Каждый запуск сценария получает новый `ExecutionContext`, а singleton-компоненты движка переиспользуются.

## 3. Этап второй: ручка запуска сценария

### 3.1. HTTP-запрос

Контроллер [`ScenarioExecutionController.java`](cqa-server/src/main/java/ru/cqa/server/web/scenario/ScenarioExecutionController.java) обрабатывает:

```http
POST /scenarios/{scenarioId}/executions
Content-Type: application/json
```

Пример тела для текущего сценария:

```json
{
  "version": "1.0",
  "environmentId": "DEV",
  "variables": {}
}
```

`scenarioId` берётся из URL. `version` и `environmentId` обязательны (`@NotBlank`). `variables` можно не передавать; контроллер заменит `null` на пустую карту.

В нашем запуске использовались ID `550e8400-e29b-41d4-a716-446655440000` и версия `1.0`.

### 3.2. Контроллер создаёт параметры запуска

Метод `execute()`:

1. Spring преобразует `scenarioId` в `UUID` и JSON тела в `ScenarioExecutionRequest`.
2. `@Valid` запускает валидацию тела запроса.
3. Контроллер создаёт `ExecutionParameters(version, environmentId, variables)`.
4. Вызывает `scenarioEngine.execute(scenarioId, parameters)`.
5. Оборачивает полученный `ExecutionResult` в `ResponseEntity.ok(...)`.

Контроллер не содержит саму логику HTTP Move, Assertions или Extracts — это передано движку.

### 3.3. Движок находит сценарий

В [`DefaultScenarioEngine.execute()`](cqa-scenario-engine/src/main/java/ru/cqa/scenario/runtime/execution/DefaultScenarioEngine.java):

1. Проверяются обязательные аргументы.
2. Вызов `repository.findByIdAndVersion(id, version)` ищет файл по ID и версии.
3. `FileScenarioRepository` читает файл и вызывает `JacksonScenarioParser.parse(...)`.
4. Парсер строит `Scenario`, читает `steps`, `moves`, Assertions и Extracts. Для каждого Move поле `type` выбирает конкретный класс; `"http"` превращается в `HttpMove`.
5. Если сценарий не найден, выбрасывается `ScenarioNotFoundException`; обработчик ручки возвращает `404`.
6. Найденный объект повторно передаётся в `ScenarioValidator.validateBody(...)`. Валидатор вызывает Jakarta Validator, собирает все нарушения и при наличии ошибок выбрасывает `ScenarioValidatorException`.

### 3.4. Создаётся контекст одного запуска

После чтения и валидации движок создаёт `ExecutionContext`:

- `executionId` — новый UUID этого запуска;
- `scenarioId` — ID сценария;
- `environmentId` — окружение из запроса;
- `variables` — входные переменные, в которые позже могут попасть результаты Extract;
- `results` — результаты Move, появляющиеся во время исполнения.

`ExecutionParameters` — входные параметры команды запуска. `ExecutionContext` — рабочее состояние, которое меняется по мере исполнения.

### 3.5. Стратегия идёт по шагам

`DefaultScenarioEngine` вызывает `SequentialExecutionStrategy.execute(scenario, context)`.

Стратегия перебирает `scenario.steps()` в том порядке, в котором они записаны в JSON. Для каждого шага вызывает `DefaultStepRunner`.

После шага стратегия смотрит на статус и политику продолжения:

- если шаг завершился `COMPLETED`, идёт дальше;
- если шаг завершился `FAILED` или `ERROR`, смотрит на `step.continueOnError` и `scenario.execution.continueAfterFailure`;
- в текущем JSON оба значения `false`, поэтому при ошибке стратегия прекратит запуск после этого шага.

### 3.6. Шаг исполняет Move

`DefaultStepRunner` проходит по `step.moves()` и передаёт каждый Move в `DefaultMoveRunner`.

`DefaultMoveRunner`:

1. Берёт тип Move через `move.getType()`.
2. Ищет executor этого типа в `MoveExecutorRegistry`.
3. Проверяет, что executor поддерживает фактический класс Move.
4. Вызывает executor и получает `MoveResult`.
5. Если executor отсутствует, вернул `null`, вернул результат для другого Move или выбросил runtime-ошибку, создаётся `MoveResult` со статусом `ERROR`.

Для текущего типа `http` в реестре находится `HttpMoveExecutor`.

### 3.7. HttpMoveExecutor отправляет запрос

В текущем `simpeScenario.json` Move содержит:

- `method`: `POST`;
- `path`: абсолютный URL `http://mdm-test.com/api/v1/users`;
- заголовки `Content-Type` и `Accept`;
- `requestBody` со строкой JSON.

`HttpMoveExecutor`:

1. Разбирает `path` как URI. Так как URL абсолютный, использует его напрямую.
2. Создаёт request factory на базе общего `HttpClient` и собирает `RestClient` для этого вызова.
3. Устанавливает HTTP-метод, URL, заголовки и тело.
4. Отправляет запрос.
5. Считывает код ответа, заголовки и тело.
6. Если тело — JSON, `JsonMapper` превращает его в `Map`/списки/простые значения; иначе оставляет строкой.
7. Формирует `HttpResponseData(statusCode, headers, body)` и помещает его в `MoveResult` со статусом `SUCCESS`.

Важная текущая семантика: `SUCCESS` означает, что HTTP-обмен состоялся и ответ получен. Сам по себе код `4xx` или `5xx` пока не переводит Move в `ERROR`; ожидаемый код проверяется Assertion-ом. Исключение при отправке запроса, например невозможность соединиться, превращается в `MoveResult.ERROR`.

Для относительного пути, например `/api/v1/users`, executor собирает URL из настройки подключения `cqa.connections.<connectionId>.<environmentId>.url`. В текущем примере путь абсолютный, поэтому URL не строится из `connectionId` и `environmentId`.

### 3.8. Выполняются Assertions

Если Move имеет статус `SUCCESS`, `DefaultStepRunner` передаёт все его Assertions в `AssertionEngine`.

`AssertionEngine` выбирает evaluator по полю `type`. Сейчас зарегистрированы:

- `equals` → `EqualsAssertionEvaluator`;
- `notNull` → `NotNullAssertionEvaluator`.

В текущем сценарии выполняются пять `equals`:

| Assertion | Откуда читается `actualExpression` | Ожидается |
|---|---|---|
| `assert-001` | `$.statusCode` — HTTP-код из `HttpResponseData` | число `200` |
| `assert-002` | `$.user.email` — тело ответа | `ivan.petrov@example.com` |
| `assert-003` | `$.user.role` — тело ответа | `USER` |
| `assert-004` | `$.user.name` — тело ответа | `Ivan` |
| `assert-005` | `$.user.surname` — тело ответа | `Petrov` |

`JsonPathReader` — небольшой собственный читатель путей, а не полноценная реализация стандарта JSONPath. Специальный путь `$.statusCode` читает настоящий HTTP-код. Все остальные пути начинаются от тела ответа, поэтому `$.user.email` ищет поле `user.email` внутри JSON body.

Каждая проверка даёт:

- `PASSED`, если значение совпало;
- `FAILED`, если значение есть, но не совпало;
- `ERROR`, если путь невозможно прочитать или проверку нельзя выполнить.

Движок выполняет все Assertions этого Move, чтобы собрать результаты по каждой проверке. Несовпадение Assertion не меняет HTTP-статус Move `SUCCESS`, но влияет на статус шага.

### 3.9. Выполняются Extracts

После Assertions `DefaultStepRunner` передаёт Extracts в `ExtractEngine`. Для типа `jsonpath` используется `JsonPathExtractor`.

В сценарии извлекаются:

- `$.user.id` → переменная `userId`;
- `$.user.email` → переменная `userEmail`.

Значения записываются в `ExecutionContext.variables` и доступны последующим Move/шагам этого же запуска. Текущий сценарий одноступенчатый, поэтому использовать эти переменные дальше пока некому. Контракт `ExecutionResult` сейчас не включает карту variables, поэтому наружу они в ответе ручки не возвращаются.

Если при выполнении Extract возникает ошибка, Move получает `ERROR`, а статус шага становится `ERROR`.

### 3.10. Собираются результаты

`DefaultStepRunner` добавляет результат Move в контекст и формирует `StepResult`:

- Assertion `FAILED` даёт шагу `FAILED`;
- Assertion `ERROR` или ошибка самого Move/Extract даёт шагу `ERROR`;
- если ошибок и несовпадений нет — `COMPLETED`.

Затем `SequentialExecutionStrategy` формирует `ExecutionResult`:

- `executionId`;
- `scenarioId`;
- общий статус `COMPLETED`, `FAILED` или `ERROR`;
- результаты выполненных шагов, Move и Assertions;
- длительность выполнения.

Контроллер сериализует результат в JSON и возвращает HTTP `200`, когда сам запуск обработан и результат сформирован. Поэтому HTTP `200` ручки не равен статусу сценария: сценарий может вернуть в теле `FAILED` или `ERROR`.

## 4. Что было при прогоне с mock MDM

Для безопасной демонстрации был запущен локальный mock на `127.0.0.1`. Временная копия сохранённого сценария указывала на mock вместо внешнего `mdm-test.com`; исходный JSON в проекте не менялся во время того прогона.

Mock получил запрос с `nameSuorce`, email и role, вернул HTTP `200` и JSON с `user.id`, `email`, `role`, `name`, `surname`. Движок выдал `ExecutionResult` со статусом `COMPLETED`, и все пять Assertions завершились `PASSED`.

После этого Assertion статуса обновлён: он использует `$.statusCode` и числовое `200`. В файле сценария `simpeScenario.json` и в сохранённом файле сценария изменение уже отражено. Повторный прогон после этого изменения отдельно не выполнялся.

## 5. Текущие границы MVP

- Исполнитель зарегистрирован только для HTTP Move. SQL, Kafka и SOAP пока не подключены к runtime.
- `execution.timeout` читается из JSON, но общий timeout сценария стратегия пока не применяет.
- `HttpMoveExecutor` использует общий `HttpClient`, но создаёт `RestClient` и request factory на каждый Move.
- Абсолютный URL из сценария используется напрямую. Для разделения сценария и окружений предпочтительнее относительный `path` и внешняя настройка подключения.
- Assertions сравнивают типизированные значения без приведения типов. Поэтому HTTP-код проверяется числом `200`, а не строкой `"200"`.
- Результаты выполнения формируются и возвращаются ручкой; запись execution history в журнал пока не показана в этой цепочке.

## 6. Основные файлы по потоку

| Этап | Файл |
|---|---|
| Точка входа | `cqa-server/src/main/java/ru/cqa/server/CqaServerApplication.java` |
| Настройка HTTP-клиента | `cqa-server/src/main/java/ru/cqa/server/config/ExternalServicesConfiguration.java` |
| Сборка сценарного runtime | `cqa-server/src/main/java/ru/cqa/server/scenario/ScenarioEngineConfiguration.java` |
| Создание / чтение сценария | `cqa-server/src/main/java/ru/cqa/server/scenario/FileScenarioRepository.java` |
| JSON → Scenario | `cqa-server/src/main/java/ru/cqa/server/scenario/JacksonScenarioParser.java` |
| Ручка запуска | `cqa-server/src/main/java/ru/cqa/server/web/scenario/ScenarioExecutionController.java` |
| Координация запуска | `cqa-scenario-engine/src/main/java/ru/cqa/scenario/runtime/execution/DefaultScenarioEngine.java` |
| Последовательное исполнение | `cqa-scenario-engine/src/main/java/ru/cqa/scenario/runtime/execution/SequentialExecutionStrategy.java` |
| Исполнение шага | `cqa-scenario-engine/src/main/java/ru/cqa/scenario/runtime/execution/DefaultStepRunner.java` |
| Выбор executor | `cqa-scenario-engine/src/main/java/ru/cqa/scenario/runtime/execution/DefaultMoveRunner.java` |
| HTTP-запрос | `cqa-server/src/main/java/ru/cqa/server/scenario/HttpMoveExecutor.java` |
| Assertion по равенству | `cqa-scenario-engine/src/main/java/ru/cqa/scenario/runtime/assertion/EqualsAssertionEvaluator.java` |
| Чтение expression | `cqa-scenario-engine/src/main/java/ru/cqa/scenario/runtime/expression/JsonPathReader.java` |
| Extract | `cqa-scenario-engine/src/main/java/ru/cqa/scenario/extract/ExtractEngine.java` |
| Модели результата | `cqa-scenario-engine/src/main/java/ru/cqa/scenario/runtime/result/` |
