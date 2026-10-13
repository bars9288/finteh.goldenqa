--  Журнал запросов CQA Server — скрипт инициализации схемы (SQLite)
--  foreign_keys=true (задано в spring.datasource.url).


CREATE TABLE IF NOT EXISTS journal_request (                      -- входящий запрос к ЕТ и итоговый ответ клиенту
    id                 TEXT PRIMARY KEY,                          -- UUID запроса, генерирует приложение
    received_at        TEXT NOT NULL,                             -- когда запрос пришёл в ЕТ
    finished_at        TEXT,                                      -- когда ответ клиенту отправлен (или обработка прервана)
    client_ip          TEXT,                                      -- адрес источника; за балансировщиком — его адрес, клиент в X-Forwarded-For
    client_port        INTEGER,                                   -- порт источника
    http_method        TEXT NOT NULL,                             -- HTTP-метод
    endpoint           TEXT NOT NULL,                             -- ручка ЕТ, например /api/saveAndMerge
    query_string       TEXT,                                      -- строка параметров запроса, без ?
    req_headers        TEXT CHECK (json_valid(req_headers)),      -- {"имя": ["значение", ...]}
    req_content_type   TEXT,                                      -- Content-Type запроса как прислал клиент
    req_body_kind      TEXT NOT NULL,                             -- EMPTY / JSON / XML / TEXT / BINARY
    req_body           TEXT,                                      -- тело запроса как пришло; сжатое (Content-Encoding gzip/deflate) — распакованным
    validation_status  TEXT,                                      -- PASSED / FAILED / NOT_RUN
    validation_errors  TEXT CHECK (json_valid(validation_errors)),-- ["...", ...]
    internal_error     TEXT,                                      -- ошибка самого ЕТ, если была
    resp_status        INTEGER,                                   -- HTTP-статус, отданный клиенту
    resp_headers       TEXT    CHECK (json_valid(resp_headers)),  -- заголовки ответа клиенту
    resp_body          TEXT,                                      -- тело ответа клиенту
    delivered          INTEGER CHECK (delivered IN (0, 1)),       -- ответ записан клиенту без ошибки
    delivery_error     TEXT                                       -- ошибка отправки ответа, например клиент оборвал соединение
);


CREATE INDEX IF NOT EXISTS ix_journal_request_received_at ON journal_request (received_at);


CREATE TABLE IF NOT EXISTS journal_call (                   -- исходящий вызов внешнего сервиса (МДМ): 0..N на запрос
    id                 TEXT PRIMARY KEY,                     -- UUID вызова, генерирует приложение
    request_id         TEXT NOT NULL REFERENCES journal_request (id),-- входящий запрос
    service            TEXT NOT NULL,                        -- вызываемый сервис, например MDM
    sent_at            TEXT NOT NULL,                        -- когда запрос отправлен сервису
    finished_at        TEXT,                                 -- когда получен ответ или ошибка
    target_url         TEXT NOT NULL,                        -- полный адрес назначения
    req_headers        TEXT CHECK (json_valid(req_headers)), -- реально ушедшие заголовки (после фильтра hop-by-hop)
    resp_status        INTEGER,                              -- HTTP-статус ответа сервиса
    resp_headers       TEXT CHECK (json_valid(resp_headers)),-- заголовки ответа сервиса
    resp_content_type  TEXT,                                 -- Content-Type ответа сервиса
    resp_body_kind     TEXT,                                 -- EMPTY / JSON / XML / TEXT / BINARY
    resp_body          TEXT,                                 -- тело ответа сервиса как пришло; сжатое (gzip/deflate) — распакованным
    call_error         TEXT                                  -- сервис не ответил: таймаут, нет соединения
);


CREATE INDEX IF NOT EXISTS ix_journal_call_request_id ON journal_call (request_id);
