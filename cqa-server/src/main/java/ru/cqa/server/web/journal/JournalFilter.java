package ru.cqa.server.web.journal;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import ru.cqa.server.db.sqlite.journal.JournalWriter;

/**
 * Журнал входящих REST-запросов: что пришло и что ушло клиенту.
 *
 * <p>Фильтр, а не код в контроллерах: он видит каждый запрос целиком — и тот,
 * что упал до контроллера (кривой JSON, нет такой ручки), и итоговый ответ
 * после всех {@code @ExceptionHandler}.
 *
 * <p>Порядок для одного запроса:
 * <ol>
 *   <li>тело читается целиком, запрос пишется в журнал ({@code requestReceived});</li>
 *   <li>id записи кладётся в атрибут {@link #REQUEST_ID} — по нему контроллер
 *       дописывает итог проверки, а клиент МДМ — свой вызов;</li>
 *   <li>обработка идёт дальше, ответ копится в памяти;</li>
 *   <li>ответ отправляется клиенту и пишется в журнал ({@code responseSent}).</li>
 * </ol>
 *
 * <p>Журнал не ломает обработку: {@link JournalWriter} сам не бросает исключений
 * из-за БД, а любой неожиданный сбой журнала здесь только логируется.
 * Обработка не ломает журнал: ответ пишется в {@code finally} — при любом исходе.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class JournalFilter extends OncePerRequestFilter {

    /**
     * Атрибут запроса с id записи журнала ({@link UUID}); нет атрибута — запрос не журналируется.
     * Литерал, а не {@code getName() + ...}: используется в аннотации {@code @RequestAttribute},
     * а там допустимы только константы времени компиляции.
     */
    public static final String REQUEST_ID = "ru.cqa.server.web.journal.JournalFilter.requestId";

    private static final Logger log = LoggerFactory.getLogger(JournalFilter.class);

    private final JournalWriter journal;

    /**
     * Служебные адреса — не REST-ручки, в журнал не пишутся: документация API,
     * её статика, SOAP (свой сервлет на spring.webservices.path) и страница ошибки.
     */
    private final List<String> notJournaled;

    public JournalFilter(JournalWriter journal, @Value("${spring.webservices.path}") String soapPath) {
        this.journal = journal;
        this.notJournaled = List.of(
                "/swagger-ui", "/v3/api-docs", "/scalar", "/webjars", soapPath + "/", "/favicon.ico", "/error");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return notJournaled.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Instant receivedAt = Instant.now();
        HttpHeaders requestHeaders = new ServletServerHttpRequest(request).getHeaders();

        byte[] body;
        try {
            body = request.getInputStream().readAllBytes();
        } catch (IOException e) {
            // клиент оборвал передачу тела — запрос всё равно попадает в журнал
            UUID id = requestReceived(request, receivedAt, requestHeaders, null);
            safely(() -> journal.internalError(id, "Тело запроса не прочитано: " + e));
            throw e;
        }

        UUID id = requestReceived(request, receivedAt, requestHeaders, body);
        if (id != null) {
            request.setAttribute(REQUEST_ID, id);
        }

        ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);
        try {
            chain.doFilter(new CachedBodyRequest(request, body), cachingResponse);
        } catch (IOException | ServletException | RuntimeException e) {
            // исключение не обработал ни один @ExceptionHandler: ответ 500 Tomcat
            // сформирует уже после фильтра, тела его здесь нет
            safely(() -> journal.internalError(id, "Необработанное исключение: " + e));
            safely(() -> journal.responseSent(id, Instant.now(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new HttpHeaders(), null, null));
            throw e;
        }
        sendResponse(id, response, cachingResponse);
    }

    /**
     * Отправляет накопленный ответ клиенту и пишет его в журнал.
     *
     * <p>{@code flushBuffer()} — чтобы отправка случилась здесь, а не после фильтра:
     * иначе обрыв соединения клиентом журнал бы не увидел.
     */
    private void sendResponse(UUID id, HttpServletResponse response, ContentCachingResponseWrapper cachingResponse)
            throws IOException {
        // до copyBodyToResponse: после него кэш тела пуст
        byte[] responseBody = cachingResponse.getContentAsByteArray();
        int status = cachingResponse.getStatus();
        HttpHeaders responseHeaders = responseHeaders(cachingResponse);
        String deliveryError = null;
        try {
            cachingResponse.copyBodyToResponse();
            response.flushBuffer();
        } catch (IOException e) {
            deliveryError = e.toString();
            throw e;
        } finally {
            String error = deliveryError;
            safely(() -> journal.responseSent(id, Instant.now(), status, responseHeaders, responseBody, error));
        }
    }

    private UUID requestReceived(HttpServletRequest request, Instant receivedAt, HttpHeaders headers, byte[] body) {
        try {
            return journal.requestReceived(receivedAt, request.getRemoteAddr(), request.getRemotePort(),
                    request.getMethod(), request.getRequestURI(), request.getQueryString(), headers, body);
        } catch (RuntimeException e) {
            log.warn("Журнал: запрос не записан, обработка идёт дальше", e);
            return null;
        }
    }

    /**
     * Заголовки ответа. Content-Type Tomcat хранит отдельно от остальных и
     * добавляет к ним только при отправке — поэтому он дописывается явно.
     */
    private static HttpHeaders responseHeaders(HttpServletResponse response) {
        HttpHeaders headers = new HttpHeaders();
        for (String name : response.getHeaderNames()) {
            headers.addAll(name, List.copyOf(response.getHeaders(name)));
        }
        if (response.getContentType() != null && !headers.containsHeader(HttpHeaders.CONTENT_TYPE)) {
            headers.add(HttpHeaders.CONTENT_TYPE, response.getContentType());
        }
        return headers;
    }

    /** Неожиданный сбой журнала не должен ломать ответ клиенту. */
    private static void safely(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            log.warn("Журнал: сбой записи, обработка идёт дальше", e);
        }
    }
}
