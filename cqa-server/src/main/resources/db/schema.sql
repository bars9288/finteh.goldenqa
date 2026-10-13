-- =============================================================================
--  Журнал запросов CQA Server — скрипт инициализации схемы (SQLite)
--
--  Выполняется при каждом старте приложения (класс JournalSchema),
--  поэтому всё создаётся через IF NOT EXISTS: на пустой БД скрипт создаёт
--  таблицы, на уже инициализированной — ничего не делает.
--
--  Ограничение такого подхода: изменить существующую таблицу (новая колонка,
--  другой тип) этот скрипт не сможет — правка IF NOT EXISTS на готовой БД
--  молча пропускается. Для изменений схемы нужен переход на миграции.
--
--  Типы SQLite: своих UUID, времени с поясом, JSON и BOOLEAN у него нет.
--    - идентификатор — UUID текстом, генерирует приложение;
--    - время — TEXT в ISO-8601 со смещением, по Москве:
--      2026-10-12T14:30:00.123+03:00 — пояс виден в самом значении;
--    - JSON — TEXT с проверкой json_valid(): кривой JSON не запишется;
--    - флаг — INTEGER 0/1.
--  Внешние ключи SQLite проверяет, только если в соединении включено
--  foreign_keys=true (задано в spring.datasource.url).
--
--  Комментарии к колонкам — в самом DDL: COMMENT ON в SQLite нет, а текст
--  CREATE TABLE вместе с комментариями хранится в БД (таблица sqlite_schema).
-- =============================================================================


CREATE TABLE IF NOT EXISTS journal_request (     -- входящий запрос к ЕТ и итоговый ответ клиенту
    id                 TEXT    PRIMARY KEY,      -- UUID запроса, генерирует приложение
    received_at        TEXT    NOT NULL,         -- когда запрос пришёл в ЕТ
    finished_at        TEXT,                     -- когда ответ клиенту отправлен (или обработка прервана)
    client_ip          TEXT,                     -- адрес источника; за балансировщиком — его адрес, клиент в X-Forwarded-For
    client_port        INTEGER,                  -- порт источника
    http_method        TEXT    NOT NULL,         -- HTTP-метод
    endpoint           TEXT    NOT NULL,         -- ручка ЕТ, например /api/saveAndMerge
    query_string       TEXT,                     -- строка параметров запроса, без ?
    req_headers        TEXT    CHECK (json_valid(req_headers)),        -- {"имя": ["значение", ...]}
    req_content_type   TEXT,                     -- Content-Type запроса как прислал клиент
    req_body_kind      TEXT    NOT NULL,         -- EMPTY / JSON / XML / TEXT / BINARY
    req_body           TEXT,                     -- тело запроса как пришло; сжатое (Content-Encoding gzip/deflate) — распакованным
    validation_status  TEXT,                     -- PASSED / FAILED / NOT_RUN
    validation_errors  TEXT    CHECK (json_valid(validation_errors)),  -- ["...", ...]
    internal_error     TEXT,                     -- ошибка самого ЕТ, если была
    resp_status        INTEGER,                  -- HTTP-статус, отданный клиенту
    resp_headers       TEXT    CHECK (json_valid(resp_headers)),       -- заголовки ответа клиенту
    resp_body          TEXT,                     -- тело ответа клиенту
    delivered          INTEGER CHECK (delivered IN (0, 1)),            -- ответ записан клиенту без ошибки
    delivery_error     TEXT                      -- ошибка отправки ответа, например клиент оборвал соединение
);

CREATE INDEX IF NOT EXISTS ix_journal_request_received_at ON journal_request (received_at);


-- Тело запроса к сервису отдельно не хранится: прокси пересылает его
-- байт в байт, оно равно journal_request.req_body.

CREATE TABLE IF NOT EXISTS journal_call (        -- исходящий вызов внешнего сервиса (МДМ): 0..N на запрос
    id                 TEXT    PRIMARY KEY,      -- UUID вызова, генерирует приложение
    request_id         TEXT    NOT NULL REFERENCES journal_request (id),  -- входящий запрос
    service            TEXT    NOT NULL,         -- вызываемый сервис, например MDM
    sent_at            TEXT    NOT NULL,         -- когда запрос отправлен сервису
    finished_at        TEXT,                     -- когда получен ответ или ошибка
    target_url         TEXT    NOT NULL,         -- полный адрес назначения
    req_headers        TEXT    CHECK (json_valid(req_headers)),        -- реально ушедшие заголовки (после фильтра hop-by-hop)
    resp_status        INTEGER,                  -- HTTP-статус ответа сервиса
    resp_headers       TEXT    CHECK (json_valid(resp_headers)),       -- заголовки ответа сервиса
    resp_content_type  TEXT,                     -- Content-Type ответа сервиса
    resp_body_kind     TEXT,                     -- EMPTY / JSON / XML / TEXT / BINARY
    resp_body          TEXT,                     -- тело ответа сервиса как пришло; сжатое (gzip/deflate) — распакованным
    call_error         TEXT                      -- сервис не ответил: таймаут, нет соединения
);

CREATE INDEX IF NOT EXISTS ix_journal_call_request_id ON journal_call (request_id);
