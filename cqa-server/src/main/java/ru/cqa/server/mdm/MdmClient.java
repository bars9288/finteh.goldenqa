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

    private static final Set<String> NOT_FORWARDED = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization", "proxy-connection",
            "te", "trailer", "transfer-encoding", "upgrade", "host", "content-length", "expect");

    private static final String SERVICE = "MDM";

    private final RestClient restClient;
    private final String saveAndMergePath;
    /** Полный адрес — только для журнала; запрос идёт по пути относительно baseUrl клиента. */
    private final String saveAndMergeUrl;
    private final JournalWriter journal;

    public MdmClient(RestClient restClient, String baseUrl, String saveAndMergePath, JournalWriter journal) {
        this.restClient = restClient;
        this.saveAndMergePath = saveAndMergePath;
        this.saveAndMergeUrl = UriComponentsBuilder.fromUriString(baseUrl).path(saveAndMergePath).toUriString();
        this.journal = journal;
    }

    /**
     * @param journalId id записи журнала входящего запроса; null — вызов не журналируется
     */
    public ResponseEntity<byte[]> saveAndMerge(UUID journalId, HttpHeaders requestHeaders, byte[] body) {
        HttpHeaders sent = forwardable(requestHeaders);
        UUID callId = journal.callStarted(journalId, SERVICE, Instant.now(), saveAndMergeUrl, sent);
        ResponseEntity<byte[]> answer;
        try {
            answer = restClient.post()
                    .uri(saveAndMergePath)
                    .headers(headers -> headers.addAll(sent))
                    .body(body)
                    .exchange((request, response) -> ResponseEntity.status(response.getStatusCode())
                            .headers(response.getHeaders())
                            .body(response.getBody().readAllBytes()));
        } catch (RestClientException e) {
            journal.callFailed(callId, Instant.now(), e.getMessage());
            throw e;
        }
        journal.callAnswered(callId, Instant.now(), answer.getStatusCode().value(), answer.getHeaders(), answer.getBody());
        return ResponseEntity.status(answer.getStatusCode())
                .headers(forwardable(answer.getHeaders()))
                .body(answer.getBody());
    }

    /** Копия заголовков без hop-by-hop и без перечисленных в самом {@code Connection}. */
    private static HttpHeaders forwardable(HttpHeaders source) {
        Set<String> skip = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        skip.addAll(NOT_FORWARDED);
        for (String value : source.getOrEmpty(HttpHeaders.CONNECTION)) {
            for (String name : value.split(",")) {
                skip.add(name.strip().toLowerCase(Locale.ROOT));
            }
        }
        HttpHeaders result = new HttpHeaders();
        source.forEach((name, values) -> {
            if (!skip.contains(name)) {
                result.addAll(name, values);
            }
        });
        return result;
    }
}
