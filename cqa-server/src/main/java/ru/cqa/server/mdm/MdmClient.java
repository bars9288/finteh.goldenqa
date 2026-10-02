package ru.cqa.server.mdm;

import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

/**
 * Клиент МДМ (CDI): пересылает проверенный запрос saveAndMerge как прозрачный прокси.
 *
 * <p>В МДМ уходят тело байт в байт и заголовки клиента (включая его {@code Authorization});
 * клиенту возвращаются статус, заголовки и тело МДМ — будто он обратился в МДМ напрямую.
 * МДМ мы не проверяем, поэтому её 4xx/5xx — не исключение, а ответ, который получит клиент.
 *
 * <p>Не пересылаются только заголовки соединения (hop-by-hop, RFC 9110 §7.6.1) и те,
 * что HTTP-клиент и сервер выставляют сами под новое соединение: {@code Host}, {@code Content-Length}.
 */
public class MdmClient {

    private static final Set<String> NOT_FORWARDED = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization", "proxy-connection",
            "te", "trailer", "transfer-encoding", "upgrade", "host", "content-length", "expect");

    private final RestClient restClient;
    private final String saveAndMergePath;

    public MdmClient(RestClient restClient, String saveAndMergePath) {
        this.restClient = restClient;
        this.saveAndMergePath = saveAndMergePath;
    }

    public ResponseEntity<byte[]> saveAndMerge(HttpHeaders requestHeaders, byte[] body) {
        return restClient.post()
                .uri(saveAndMergePath)
                .headers(headers -> headers.addAll(forwardable(requestHeaders)))
                .body(body)
                .exchange((request, response) -> ResponseEntity.status(response.getStatusCode())
                        .headers(forwardable(response.getHeaders()))
                        .body(response.getBody().readAllBytes()));
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
