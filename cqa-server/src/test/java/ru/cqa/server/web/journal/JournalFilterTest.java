package ru.cqa.server.web.journal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.cqa.cases.validation.SaveAndMergeValidationException;
import ru.cqa.cases.validation.SaveAndMergeValidator;
import ru.cqa.server.web.ApiPaths;

/**
 * Журнал на настоящем HTTP: приложение на случайном порту, БД — временный файл,
 * МДМ — поддельный HTTP-сервер из JDK.
 *
 * <p>Валидатор подменён: здесь проверяется журнал, а не правила saveAndMerge.
 * Каждый запрос помечается параметром {@code ?t=<uuid>} — по нему ищется его запись.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.config.additional-location=file:../")
class JournalFilterTest {

    @TempDir
    static Path dir;

    /**
     * Поддельная МДМ: отвечает 201; с заголовком X-Test-Slow молчит дольше таймаута чтения,
     * с X-Test-Gzip — сжимает ответ, как настоящая МДМ на {@code Accept-Encoding: gzip}.
     */
    static HttpServer mdm;

    /** Заголовок Authorization, дошедший до МДМ в последнем запросе. */
    static volatile String mdmSawAuthorization;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) throws IOException {
        mdm = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        mdm.createContext("/saveAndMerge", exchange -> {
            mdmSawAuthorization = exchange.getRequestHeaders().getFirst("Authorization");
            exchange.getRequestBody().readAllBytes();
            if (exchange.getRequestHeaders().containsKey("X-Test-Slow")) {
                sleep(3000);
            }
            byte[] answer = "{\"mdm\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
            if (exchange.getRequestHeaders().containsKey("X-Test-Gzip")) {
                answer = gzip(answer);
                exchange.getResponseHeaders().add("Content-Encoding", "gzip");
            }
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.getResponseHeaders().add("X-Mdm", "yes");
            exchange.sendResponseHeaders(201, answer.length);
            exchange.getResponseBody().write(answer);
            exchange.close();
        });
        mdm.start();

        registry.add("spring.datasource.url",
                () -> "jdbc:sqlite:" + dir.resolve("journal.db") + "?foreign_keys=true&journal_mode=WAL&busy_timeout=5000");
        registry.add("cqa.journal.fallback-dir", () -> dir.resolve("fallback").toString());
        // connections.yml в тесте не подключается — контур МДМ задан здесь
        String cdi = "cqa.connections.CDI.DEV.";
        registry.add(cdi + "url", () -> "http://127.0.0.1:" + mdm.getAddress().getPort());
        registry.add(cdi + "connect-timeout", () -> "2s");
        registry.add(cdi + "read-timeout", () -> "1s");
        registry.add(cdi + "endpoints.save-and-merge", () -> "/saveAndMerge");
    }

    @AfterAll
    static void stopMdm() {
        mdm.stop(0);
    }

    @MockitoBean
    SaveAndMergeValidator validator;

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbc;

    final HttpClient http = HttpClient.newHttpClient();

    @Test
    void echoRequestAndResponseAreJournaled() throws Exception {
        String tag = tag();
        HttpResponse<String> response = post("/echo?" + tag, "text/plain", "привет", Map.of("X-Client", "test"));

        assertThat(response.statusCode()).isEqualTo(200);
        Map<String, Object> row = journalRow(tag);
        assertThat(row.get("http_method")).isEqualTo("POST");
        assertThat(row.get("endpoint")).isEqualTo("/echo");
        assertThat(row.get("client_ip")).isEqualTo("127.0.0.1");
        assertThat(row.get("client_port")).isNotNull();
        assertThat((String) row.get("req_headers")).contains("\"X-Client\":[\"test\"]");
        assertThat(row.get("req_body_kind")).isEqualTo("TEXT");
        assertThat(row.get("req_body")).isEqualTo("привет");
        assertThat(row.get("validation_status")).isEqualTo("NOT_RUN");
        assertThat(row.get("resp_status")).isEqualTo(200);
        assertThat(row.get("resp_body")).isEqualTo("привет");
        assertThat((String) row.get("resp_headers")).contains("Content-Type");
        assertThat(row.get("delivered")).isEqualTo(1);
        assertThat(row.get("finished_at")).isNotNull();
    }

    @Test
    void validSaveAndMergeIsJournaledWithMdmCall() throws Exception {
        String tag = tag();
        HttpResponse<String> response = post("/" + ApiPaths.SAVE_AND_MERGE + "?" + tag, "application/json",
                "{\"party\":[]}", Map.of("Authorization", "Bearer secret-token"));

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).isEqualTo("{\"mdm\":\"ok\"}");
        assertThat(mdmSawAuthorization).isEqualTo("Bearer secret-token");

        Map<String, Object> row = journalRow(tag);
        assertThat(row.get("req_body_kind")).isEqualTo("JSON");
        assertThat(row.get("validation_status")).isEqualTo("PASSED");
        assertThat(row.get("resp_status")).isEqualTo(201);
        assertThat(row.get("resp_body")).isEqualTo("{\"mdm\":\"ok\"}");
        // заголовки хранятся как есть, без маскирования
        assertThat((String) row.get("req_headers")).contains("Bearer secret-token");

        Map<String, Object> call = jdbc.queryForMap("SELECT * FROM journal_call WHERE request_id = ?", row.get("id"));
        assertThat(call.get("service")).isEqualTo("MDM");
        assertThat((String) call.get("target_url")).endsWith("/saveAndMerge");
        assertThat((String) call.get("req_headers")).contains("Bearer secret-token");
        assertThat(call.get("resp_status")).isEqualTo(201);
        // JDK HttpClient, через который ходит MdmClient, отдаёт имена заголовков в нижнем регистре
        assertThat((String) call.get("resp_headers")).contains("\"x-mdm\":[\"yes\"]");
        assertThat(call.get("resp_body_kind")).isEqualTo("JSON");
        assertThat(call.get("call_error")).isNull();
    }

    @Test
    void gzipAnswerReachesClientAsIsAndJournalUnpacked() throws Exception {
        String tag = tag();
        HttpRequest request = HttpRequest.newBuilder(uri("/" + ApiPaths.SAVE_AND_MERGE + "?" + tag))
                .header("Content-Type", "application/json")
                .header("X-Test-Gzip", "1")
                .POST(HttpRequest.BodyPublishers.ofString("{\"party\":[]}"))
                .build();
        HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());

        // клиенту — сжатые байты МДМ без изменений
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.headers().firstValue("Content-Encoding")).hasValue("gzip");
        assertThat(response.body()).isEqualTo(gzip("{\"mdm\":\"ok\"}".getBytes(StandardCharsets.UTF_8)));

        // в журнал — распакованный текст
        Map<String, Object> row = journalRow(tag);
        assertThat(row.get("resp_body")).isEqualTo("{\"mdm\":\"ok\"}");
        Map<String, Object> call = jdbc.queryForMap("SELECT * FROM journal_call WHERE request_id = ?", row.get("id"));
        assertThat(call.get("resp_body_kind")).isEqualTo("JSON");
        assertThat(call.get("resp_body")).isEqualTo("{\"mdm\":\"ok\"}");
        assertThat((String) call.get("resp_headers")).contains("\"content-encoding\":[\"gzip\"]");
    }

    private static byte[] gzip(byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(data);
        }
        return out.toByteArray();
    }

    @Test
    void invalidSaveAndMergeIsJournaledWithErrors() throws Exception {
        doThrow(new SaveAndMergeValidationException(List.of("party: не должно быть пустым")))
                .when(validator).validate(any());
        String tag = tag();
        HttpResponse<String> response = post("/" + ApiPaths.SAVE_AND_MERGE + "?" + tag, "application/json",
                "{\"party\":[]}", Map.of());

        assertThat(response.statusCode()).isEqualTo(400);
        Map<String, Object> row = journalRow(tag);
        assertThat(row.get("validation_status")).isEqualTo("FAILED");
        assertThat(row.get("validation_errors")).isEqualTo("[\"party: не должно быть пустым\"]");
        assertThat(row.get("resp_status")).isEqualTo(400);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM journal_call WHERE request_id = ?",
                Integer.class, row.get("id"))).isZero();
    }

    @Test
    void mdmTimeoutIsJournaledAsCallError() throws Exception {
        String tag = tag();
        HttpResponse<String> response = post("/" + ApiPaths.SAVE_AND_MERGE + "?" + tag, "application/json",
                "{\"party\":[]}", Map.of("X-Test-Slow", "1"));

        assertThat(response.statusCode()).isEqualTo(502);
        Map<String, Object> row = journalRow(tag);
        assertThat(row.get("resp_status")).isEqualTo(502);
        Map<String, Object> call = jdbc.queryForMap("SELECT * FROM journal_call WHERE request_id = ?", row.get("id"));
        assertThat(call.get("call_error")).isNotNull();
        assertThat(call.get("resp_status")).isNull();
        assertThat(call.get("finished_at")).isNotNull();
    }

    @Test
    void unparsableBodyIsJournaledWithoutValidation() throws Exception {
        String tag = tag();
        HttpResponse<String> response = post("/" + ApiPaths.SAVE_AND_MERGE + "?" + tag, "application/json",
                "{\"party\":", Map.of());

        assertThat(response.statusCode()).isEqualTo(400);
        Map<String, Object> row = journalRow(tag);
        assertThat(row.get("req_body_kind")).isEqualTo("TEXT");
        assertThat(row.get("validation_status")).isEqualTo("NOT_RUN");
        assertThat(row.get("resp_status")).isEqualTo(400);
    }

    @Test
    void unknownPathIsJournaledAndClientStillGetsErrorBody() throws Exception {
        String tag = tag();
        HttpResponse<String> response = http.send(
                HttpRequest.newBuilder(uri("/no-such-handler?" + tag)).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(404);
        // страницу ошибки Spring Boot рисует после фильтра — фильтр не должен ей помешать
        assertThat(response.body()).isNotBlank();
        assertThat(journalRow(tag).get("resp_status")).isEqualTo(404);
    }

    @Test
    void apiDocsAreNotJournaled() throws Exception {
        String tag = tag();
        HttpResponse<String> response = http.send(
                HttpRequest.newBuilder(uri("/v3/api-docs?" + tag)).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        sleep(300);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM journal_request WHERE query_string = ?",
                Integer.class, tag)).isZero();
    }

    private HttpResponse<String> post(String path, String contentType, String body, Map<String, String> headers)
            throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        headers.forEach(request::header);
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + port + path);
    }

    private static String tag() {
        return "t=" + UUID.randomUUID();
    }

    /**
     * Запись журнала по метке. Ответ пишется в журнал сразу после отправки клиенту,
     * то есть клиент может получить его чуть раньше, — поэтому ждём finished_at.
     */
    private Map<String, Object> journalRow(String tag) {
        for (int attempt = 0; attempt < 50; attempt++) {
            List<Map<String, Object>> rows = jdbc.queryForList(
                    "SELECT * FROM journal_request WHERE query_string = ? AND finished_at IS NOT NULL", tag);
            if (!rows.isEmpty()) {
                return rows.get(0);
            }
            sleep(100);
        }
        throw new AssertionError("В журнале нет завершённой записи " + tag);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
