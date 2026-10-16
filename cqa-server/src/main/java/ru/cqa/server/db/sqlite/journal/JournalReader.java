package ru.cqa.server.db.sqlite.journal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import com.fasterxml.jackson.annotation.JsonRawValue;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Чтение журнала запросов — для веб-интерфейса и разбора полётов.
 *
 * <p>Пара к {@link JournalWriter}: тот единственный пишет, этот только читает.
 * Запросы — обычный SQL через {@link JdbcClient}, а не репозитории Spring Data:
 * нужны фильтры, постраничный вывод и агрегаты, которые методами-запросами не выразить.
 *
 * <p>Если БД журнала не поднялась при старте ({@link JournalSchema}), читать нечего:
 * методы возвращают пустой результат, а {@link #ready()} — false. Ошибкой это не считается —
 * журнал в это время пишется в резервный файл.
 *
 * <p>Поля с JSON (заголовки, ошибки проверки) отдаются как есть через {@link JsonRawValue}:
 * в БД они уже лежат валидным JSON (CHECK json_valid), разбирать и собирать их заново незачем.
 */
@Component
public class JournalReader {

    /** Больше этого за один запрос не отдаём — страница журнала, а не выгрузка. */
    public static final int MAX_LIMIT = 200;

    private static final String SUMMARY_COLUMNS = """
            r.id, r.received_at, r.finished_at, r.client_ip, r.http_method, r.endpoint, r.query_string,
            r.req_body_kind, r.validation_status, r.internal_error, r.resp_status, r.delivered,
            (SELECT COUNT(*) FROM journal_call c WHERE c.request_id = r.id) AS calls,
            (SELECT c.resp_status FROM journal_call c WHERE c.request_id = r.id
              ORDER BY c.sent_at DESC LIMIT 1) AS last_call_status,
            (SELECT c.call_error FROM journal_call c WHERE c.request_id = r.id
              ORDER BY c.sent_at DESC LIMIT 1) AS last_call_error""";

    private final JdbcClient jdbc;
    private final JournalSchema schema;

    JournalReader(DataSource dataSource, JournalSchema schema) {
        this.jdbc = JdbcClient.create(dataSource);
        this.schema = schema;
    }

    /** true — журнал ведётся в БД и его можно читать; false — пишется в резервный файл. */
    public boolean ready() {
        return schema.ready();
    }

    /** Страница журнала, новые записи сверху. */
    public Page list(Filter filter) {
        if (!ready()) {
            return new Page(List.of(), 0, filter.limit(), filter.offset());
        }
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        Map<String, Object> params = new LinkedHashMap<>();
        if (filter.endpoint() != null && !filter.endpoint().isBlank()) {
            where.append(" AND r.endpoint LIKE :endpoint");
            params.put("endpoint", "%" + filter.endpoint().strip() + "%");
        }
        if (filter.validation() != null) {
            where.append(" AND r.validation_status = :validation");
            params.put("validation", filter.validation().name());
        }
        if (filter.statusClass() != null) {
            // 2 → 200..299; 0 — ответа ещё нет (запрос в обработке или упал без ответа)
            if (filter.statusClass() == 0) {
                where.append(" AND r.resp_status IS NULL");
            } else {
                where.append(" AND r.resp_status BETWEEN :statusFrom AND :statusTo");
                params.put("statusFrom", filter.statusClass() * 100);
                params.put("statusTo", filter.statusClass() * 100 + 99);
            }
        }
        if (filter.text() != null && !filter.text().isBlank()) {
            // поиск подстроки в телах: найти запрос по ФИО, rawId, тексту ошибки
            where.append(" AND (r.req_body LIKE :text OR r.resp_body LIKE :text OR r.validation_errors LIKE :text"
                    + " OR r.internal_error LIKE :text OR r.id LIKE :text)");
            params.put("text", "%" + filter.text().strip() + "%");
        }

        long total = jdbc.sql("SELECT COUNT(*) FROM journal_request r" + where)
                .params(params)
                .query(Long.class)
                .single();

        List<Summary> items = jdbc.sql("SELECT " + SUMMARY_COLUMNS + " FROM journal_request r" + where
                        + " ORDER BY r.received_at DESC LIMIT :limit OFFSET :offset")
                .params(params)
                .param("limit", filter.limit())
                .param("offset", filter.offset())
                .query(JournalReader::summary)
                .list();

        return new Page(items, total, filter.limit(), filter.offset());
    }

    /** Запись целиком: запрос, ответ клиенту и все вызовы внешних сервисов. */
    public Optional<Details> get(UUID id) {
        if (!ready()) {
            return Optional.empty();
        }
        Optional<Details> request = jdbc.sql("SELECT * FROM journal_request WHERE id = :id")
                .param("id", id.toString())
                .query((rs, n) -> new Details(
                        rs.getString("id"),
                        rs.getString("received_at"),
                        rs.getString("finished_at"),
                        durationMs(rs.getString("received_at"), rs.getString("finished_at")),
                        rs.getString("client_ip"),
                        integer(rs, "client_port"),
                        rs.getString("http_method"),
                        rs.getString("endpoint"),
                        rs.getString("query_string"),
                        rs.getString("req_headers"),
                        rs.getString("req_content_type"),
                        rs.getString("req_body_kind"),
                        rs.getString("req_body"),
                        rs.getString("validation_status"),
                        rs.getString("validation_errors"),
                        rs.getString("internal_error"),
                        integer(rs, "resp_status"),
                        rs.getString("resp_headers"),
                        rs.getString("resp_body"),
                        bool(rs, "delivered"),
                        rs.getString("delivery_error"),
                        List.of()))
                .optional();
        return request.map(r -> r.withCalls(calls(id)));
    }

    /** Сводка по всему журналу: сколько запросов и чем они закончились. */
    public Stats stats() {
        if (!ready()) {
            return new Stats(false, 0, 0, 0, 0, 0, 0, null);
        }
        return jdbc.sql("""
                        SELECT COUNT(*) AS total,
                               COALESCE(SUM(validation_status = 'PASSED'), 0) AS passed,
                               COALESCE(SUM(validation_status = 'FAILED'), 0) AS failed,
                               COALESCE(SUM(resp_status BETWEEN 500 AND 599), 0) AS server_errors,
                               COALESCE(SUM(internal_error IS NOT NULL), 0) AS internal_errors,
                               MAX(received_at) AS last_received_at,
                               (SELECT COUNT(*) FROM journal_call WHERE call_error IS NOT NULL) AS failed_calls
                          FROM journal_request""")
                .query((rs, n) -> new Stats(true,
                        rs.getLong("total"),
                        rs.getLong("passed"),
                        rs.getLong("failed"),
                        rs.getLong("server_errors"),
                        rs.getLong("internal_errors"),
                        rs.getLong("failed_calls"),
                        rs.getString("last_received_at")))
                .single();
    }

    private List<Call> calls(UUID requestId) {
        return jdbc.sql("SELECT * FROM journal_call WHERE request_id = :id ORDER BY sent_at")
                .param("id", requestId.toString())
                .query((rs, n) -> new Call(
                        rs.getString("id"),
                        rs.getString("service"),
                        rs.getString("sent_at"),
                        rs.getString("finished_at"),
                        durationMs(rs.getString("sent_at"), rs.getString("finished_at")),
                        rs.getString("target_url"),
                        rs.getString("req_headers"),
                        integer(rs, "resp_status"),
                        rs.getString("resp_headers"),
                        rs.getString("resp_content_type"),
                        rs.getString("resp_body_kind"),
                        rs.getString("resp_body"),
                        rs.getString("call_error")))
                .list();
    }

    private static Summary summary(ResultSet rs, int rowNum) throws SQLException {
        return new Summary(
                rs.getString("id"),
                rs.getString("received_at"),
                durationMs(rs.getString("received_at"), rs.getString("finished_at")),
                rs.getString("client_ip"),
                rs.getString("http_method"),
                rs.getString("endpoint"),
                rs.getString("query_string"),
                rs.getString("req_body_kind"),
                rs.getString("validation_status"),
                rs.getString("internal_error") != null,
                integer(rs, "resp_status"),
                bool(rs, "delivered"),
                rs.getInt("calls"),
                integer(rs, "last_call_status"),
                rs.getString("last_call_error"));
    }

    /** Время в БД — ISO-8601 со смещением, см. SqliteConverters. */
    private static Long durationMs(String from, String to) {
        if (from == null || to == null) {
            return null;
        }
        return Duration.between(OffsetDateTime.parse(from), OffsetDateTime.parse(to)).toMillis();
    }

    /** {@code getInt} превращает NULL в 0 — а «ответа нет» и «статус 0» здесь разные вещи. */
    private static Integer integer(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static Boolean bool(ResultSet rs, String column) throws SQLException {
        Integer value = integer(rs, column);
        return value == null ? null : value != 0;
    }

    /**
     * Условия выборки; null в поле — без фильтра.
     *
     * @param statusClass первая цифра HTTP-статуса ответа (2, 4, 5); 0 — ответа нет
     * @param text        подстрока в телах запроса/ответа, ошибках или id
     */
    public record Filter(String endpoint, ValidationStatus validation, Integer statusClass, String text,
                         int limit, int offset) {

        public Filter {
            limit = Math.clamp(limit, 1, MAX_LIMIT);
            offset = Math.max(offset, 0);
        }
    }

    public record Page(List<Summary> items, long total, int limit, int offset) {
    }

    /** Строка списка журнала — без тел и заголовков. */
    public record Summary(
            String id,
            String receivedAt,
            Long durationMs,
            String clientIp,
            String httpMethod,
            String endpoint,
            String queryString,
            String reqBodyKind,
            String validationStatus,
            boolean hasInternalError,
            Integer respStatus,
            Boolean delivered,
            int calls,
            Integer lastCallStatus,
            String lastCallError) {
    }

    public record Details(
            String id,
            String receivedAt,
            String finishedAt,
            Long durationMs,
            String clientIp,
            Integer clientPort,
            String httpMethod,
            String endpoint,
            String queryString,
            @JsonRawValue String reqHeaders,
            String reqContentType,
            String reqBodyKind,
            String reqBody,
            String validationStatus,
            @JsonRawValue String validationErrors,
            String internalError,
            Integer respStatus,
            @JsonRawValue String respHeaders,
            String respBody,
            Boolean delivered,
            String deliveryError,
            List<Call> calls) {

        Details withCalls(List<Call> calls) {
            return new Details(id, receivedAt, finishedAt, durationMs, clientIp, clientPort, httpMethod, endpoint,
                    queryString, reqHeaders, reqContentType, reqBodyKind, reqBody, validationStatus,
                    validationErrors, internalError, respStatus, respHeaders, respBody, delivered, deliveryError,
                    new ArrayList<>(calls));
        }
    }

    /** Исходящий вызов внешнего сервиса (МДМ). */
    public record Call(
            String id,
            String service,
            String sentAt,
            String finishedAt,
            Long durationMs,
            String targetUrl,
            @JsonRawValue String reqHeaders,
            Integer respStatus,
            @JsonRawValue String respHeaders,
            String respContentType,
            String respBodyKind,
            String respBody,
            String callError) {
    }

    /**
     * @param ready        false — БД журнала недоступна, остальные поля нулевые
     * @param failedCalls  вызовы внешних сервисов, на которые ответа не было (таймаут, нет соединения)
     */
    public record Stats(boolean ready, long total, long passed, long failed, long serverErrors,
                        long internalErrors, long failedCalls, String lastReceivedAt) {
    }
}
