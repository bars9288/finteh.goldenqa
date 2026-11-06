package ru.cqa.server.db.sqlite.journal;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Запись в журнал запросов — единственная точка входа для остального приложения.
 *
 * <p>Порядок записи одного входящего запроса:
 * <ol>
 *   <li>{@link #requestReceived} — сразу при приходе: строка появляется в журнале,
 *       даже если дальше обработка упадёт;</li>
 *   <li>{@link #validated}, {@link #internalError} — по ходу обработки;</li>
 *   <li>{@link #callStarted}, затем {@link #callAnswered} или {@link #callFailed} —
 *       на каждый вызов внешнего сервиса;</li>
 *   <li>{@link #responseSent} — когда ответ клиенту отправлен.</li>
 * </ol>
 *
 * <p>Принимает типы веб-слоя (заголовки, байты тела) и сам переводит их в вид для БД:
 * заголовки и ошибки — в JSON, тело — в текст с типом, см. {@link JournalBody}.
 *
 * <p>id, равный null, допустим во всех методах, кроме {@link #requestReceived}: значит,
 * запрос не журналируется (служебная ручка, тест без фильтра) — метод ничего не делает.
 * Так вызывающему коду не нужно проверять id перед каждым вызовом.
 *
 * <p>Журнал не ломает обработку запросов: методы не бросают исключений из-за БД.
 * Если БД недоступна (см. {@link JournalSchema}) или запись в неё упала, та же
 * запись уходит в резервный файл ({@link JournalFallbackFile}), а id всё равно возвращается.
 *
 * <p>{@code @Component}, а не {@code @Bean}: так репозитории и конструктор остаются
 * видны только внутри пакета, а наружу торчит один этот класс.
 */
@Component
public class JournalWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JournalWriter.class);

    private final JdbcAggregateTemplate jdbcTemplate;
    private final JournalRequestRepository requestRepository;
    private final JournalCallRepository callRepository;
    private final JournalSchema journalSchema;
    private final JournalFallbackFile fallbackFile;
    private final JsonMapper jsonMapper;

    JournalWriter(JdbcAggregateTemplate jdbcTemplate, JournalRequestRepository requestRepository, JournalCallRepository callRepository,
                  JournalSchema journalSchema, JournalFallbackFile fallbackFile, JsonMapper jsonMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.requestRepository = requestRepository;
        this.callRepository = callRepository;
        this.journalSchema = journalSchema;
        this.fallbackFile = fallbackFile;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Запрос пришёл в ЕТ.
     *
     * @return id записи — его передают во все остальные методы; есть всегда, даже если БД недоступна
     */
    public UUID requestReceived(Instant receivedAt, String clientIp, Integer clientPort,
                                String httpMethod, String endpoint, String queryString,
                                HttpHeaders headers, byte[] body) {
        String contentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);
        JournalBody journalBody = JournalBody.of(body, headers, jsonMapper);
        JournalRequest request = new JournalRequest(
                UUID.randomUUID(), receivedAt, null, clientIp, clientPort,
                httpMethod, endpoint, queryString, toJson(headers), contentType,
                journalBody.kind(), journalBody.text(), ValidationStatus.NOT_RUN, null, null,
                null, null, null, null, null);
        write("requestReceived", request, () -> jdbcTemplate.insert(request));
        return request.id();
    }

    /** Проверка выполнена; пустой список — ошибок нет. */
    public void validated(UUID requestId, List<String> errors) {
        if (requestId == null) {
            return;
        }
        ValidationStatus status = errors.isEmpty() ? ValidationStatus.PASSED : ValidationStatus.FAILED;
        String errorsJson = errors.isEmpty() ? null : toJson(errors);
        write("validated", columns("id", requestId, "validation_status", status, "validation_errors", errorsJson),
                () -> expectOne(requestRepository.updateValidation(requestId, status, errorsJson), requestId));
    }

    /** Ошибка самого ЕТ: МДМ не настроена, тело не разобралось и т.п. */
    public void internalError(UUID requestId, String error) {
        if (requestId == null) {
            return;
        }
        write("internalError", columns("id", requestId, "internal_error", error),
                () -> expectOne(requestRepository.updateInternalError(requestId, error), requestId));
    }

    /**
     * Ответ клиенту отправлен (или отправка не удалась).
     *
     * @param deliveryError null, если ответ записан клиенту без ошибки
     */
    public void responseSent(UUID requestId, Instant finishedAt, int status, HttpHeaders headers,
                             byte[] body, String deliveryError) {
        if (requestId == null) {
            return;
        }
        JournalBody journalBody = JournalBody.of(body, headers, jsonMapper);
        String headersJson = toJson(headers);
        boolean delivered = deliveryError == null;
        write("responseSent", columns("id", requestId, "finished_at", finishedAt, "resp_status", status,
                        "resp_headers", headersJson, "resp_body", journalBody.text(),
                        "delivered", delivered, "delivery_error", deliveryError),
                () -> expectOne(requestRepository.updateResponse(requestId, finishedAt, status, headersJson,
                        journalBody.text(), delivered, deliveryError), requestId));
    }

    /**
     * Запрос к внешнему сервису отправлен.
     *
     * @param headers заголовки, реально ушедшие сервису (после фильтра hop-by-hop)
     * @return id вызова — для {@link #callAnswered} или {@link #callFailed};
     *         null, только если null передан и как requestId
     */
    public UUID callStarted(UUID requestId, String service, Instant sentAt, String targetUrl, HttpHeaders headers) {
        if (requestId == null) {
            return null;
        }
        JournalCall call = new JournalCall(
                UUID.randomUUID(), requestId, service, sentAt, null, targetUrl, toJson(headers),
                null, null, null, null, null, null);
        write("callStarted", call, () -> jdbcTemplate.insert(call));
        return call.id();
    }

    /** Сервис ответил — с любым статусом, в том числе 4xx/5xx. */
    public void callAnswered(UUID callId, Instant finishedAt, int status, HttpHeaders headers, byte[] body) {
        if (callId == null) {
            return;
        }
        String contentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);
        JournalBody journalBody = JournalBody.of(body, headers, jsonMapper);
        String headersJson = toJson(headers);
        write("callAnswered", columns("id", callId, "finished_at", finishedAt, "resp_status", status,
                        "resp_headers", headersJson, "resp_content_type", contentType,
                        "resp_body_kind", journalBody.kind(), "resp_body", journalBody.text()),
                () -> expectOne(callRepository.updateAnswer(callId, finishedAt, status, headersJson, contentType,
                        journalBody.kind(), journalBody.text()), callId));
    }

    /** Сервис не ответил: таймаут, нет соединения. */
    public void callFailed(UUID callId, Instant finishedAt, String error) {
        if (callId == null) {
            return;
        }
        write("callFailed", columns("id", callId, "finished_at", finishedAt, "call_error", error),
                () -> expectOne(callRepository.updateError(callId, finishedAt, error), callId));
    }

    /**
     * Единая точка записи: в БД, а если она недоступна или запись упала — в резервный файл.
     *
     * @param data то же, что уходит в БД, — для резервного файла
     */
    private void write(String operation, Object data, Runnable databaseWrite) {
        if (!journalSchema.ready()) {
            // БД не поднялась при старте — не ждём таймаута соединения на каждой записи
            fallbackFile.write(operation, data);
            return;
        }
        try {
            databaseWrite.run();
        } catch (RuntimeException exception) {
            LOGGER.warn("Журнал: {} не записан в БД, пишу в резервный файл", operation, exception);
            fallbackFile.write(operation, data);
        }
    }

    /** Колонки обновления для резервного файла: имя, значение, имя, значение... (Map.of не терпит null). */
    private static Map<String, Object> columns(Object... namesAndValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < namesAndValues.length; i += 2) {
            map.put((String) namesAndValues[i], namesAndValues[i + 1]);
        }
        return map;
    }

    /** {"имя": ["значение", ...]} — у заголовка может быть несколько значений. */
    private String toJson(HttpHeaders headers) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        headers.forEach(map::put);
        return jsonMapper.writeValueAsString(map);
    }

    private String toJson(List<String> errors) {
        return jsonMapper.writeValueAsString(errors);
    }

    /** Ни одной строки не обновлено: записи с таким id в БД нет (например, она ушла в резервный файл). */
    private static void expectOne(int updatedRows, UUID id) {
        if (updatedRows != 1) {
            throw new IllegalStateException("В журнале нет записи " + id);
        }
    }
}
