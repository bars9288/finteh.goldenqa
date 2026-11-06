package ru.cqa.server.mdm;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import ru.cqa.server.db.sqlite.journal.JournalWriter;

/**
 * Клиент МДМ: пересылает проверенный запрос saveAndMerge как прозрачный прокси.
 *
 * <p>В МДМ уходят тело байт в байт и заголовки клиента (включая его {@code Authorization});
 * клиенту возвращаются статус, заголовки и тело МДМ — будто он обратился в МДМ напрямую.
 * МДМ мы не проверяем, поэтому её 4xx/5xx — не исключение, а ответ, который получит клиент.
 *
 * <p>Не пересылаются только заголовки соединения (hop-by-hop, RFC 9110 §7.6.1) и те,
 * что HTTP-клиент и сервер выставляют сами под новое соединение: {@code Host}, {@code Content-Length}.
 *
 * <p>Каждый вызов пишется в журнал (journal_call): ушедшие заголовки, ответ МДМ
 * как есть — до фильтрации заголовков — или ошибка, если МДМ не ответила.
 */
public class MdmClient {

    private static final Set<String> EXCLUDED_HEADER_NAMES = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization", "proxy-connection",
            "te", "trailer", "transfer-encoding", "upgrade", "host", "content-length", "expect");

    private static final String SERVICE_NAME = "MDM";

    private final RestClient restClient;
    private final String saveAndMergePath;
    /** Полный адрес — только для журнала; запрос идёт по пути относительно baseUrl клиента. */
    private final String saveAndMergeUrl;
    private final JournalWriter journalWriter;

    public MdmClient(RestClient restClient, String baseUrl, String saveAndMergePath, JournalWriter journalWriter) {
        this.restClient = restClient;
        this.saveAndMergePath = saveAndMergePath;
        this.saveAndMergeUrl = UriComponentsBuilder.fromUriString(baseUrl).path(saveAndMergePath).toUriString();
        this.journalWriter = journalWriter;
    }

    /**
     * @param journalId id записи журнала входящего запроса; null — вызов не журналируется
     */
    public ResponseEntity<byte[]> saveAndMerge(UUID journalId, HttpHeaders requestHeaders, byte[] body) {
        HttpHeaders forwardedHeaders = filterForwardableHeaders(requestHeaders);
        UUID callId = journalWriter.callStarted(journalId, SERVICE_NAME, Instant.now(), saveAndMergeUrl, forwardedHeaders);
        ResponseEntity<byte[]> mdmResponse;
        try {
            mdmResponse = restClient.post()
                    .uri(saveAndMergePath)
                    .headers(headers -> headers.addAll(forwardedHeaders))
                    .body(body)
                    .exchange((request, response) -> ResponseEntity.status(response.getStatusCode())
                            .headers(response.getHeaders())
                            .body(response.getBody().readAllBytes()));
        } catch (RestClientException exception) {
            journalWriter.callFailed(callId, Instant.now(), exception.getMessage());
            throw exception;
        }
        journalWriter.callAnswered(callId, Instant.now(), mdmResponse.getStatusCode().value(), mdmResponse.getHeaders(), mdmResponse.getBody());
        return ResponseEntity.status(mdmResponse.getStatusCode())
                .headers(filterForwardableHeaders(mdmResponse.getHeaders()))
                .body(mdmResponse.getBody());
    }

    /** Копия заголовков без hop-by-hop и без перечисленных в самом {@code Connection}. */
    private static HttpHeaders filterForwardableHeaders(HttpHeaders sourceHeaders) {
        Set<String> excludedHeaderNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        excludedHeaderNames.addAll(EXCLUDED_HEADER_NAMES);
        for (String value : sourceHeaders.getOrEmpty(HttpHeaders.CONNECTION)) {
            for (String name : value.split(",")) {
                excludedHeaderNames.add(name.strip().toLowerCase(Locale.ROOT));
            }
        }
        HttpHeaders forwardedHeaders = new HttpHeaders();
        sourceHeaders.forEach((name, values) -> {
            if (!excludedHeaderNames.contains(name)) {
                forwardedHeaders.addAll(name, values);
            }
        });
        return forwardedHeaders;
    }
}
