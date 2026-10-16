package ru.cqa.server.web.journal;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.cqa.server.web.ApiPaths;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Ручки веб-интерфейса: чтение журнала и сводка о приложении — на настоящем HTTP.
 *
 * <p>Главное, что здесь проверяется: интерфейс и SOAP не попадают в журнал сами,
 * а запросы к рабочим ручкам читаются обратно вместе с заголовками и телом.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.config.additional-location=file:../")
class JournalControllerTest {

    @TempDir
    static Path dir;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> "jdbc:sqlite:" + dir.resolve("journal.db") + "?foreign_keys=true&journal_mode=WAL&busy_timeout=5000");
        registry.add("cqa.journal.fallback-dir", () -> dir.resolve("fallback").toString());
    }

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    JsonMapper json;

    final HttpClient http = HttpClient.newHttpClient();

    @Test
    void journaledRequestIsFoundByTextAndReadInFull() throws Exception {
        String marker = "маркер-" + UUID.randomUUID();
        send("POST", "/echo", "text/plain", marker);

        JsonNode page = awaitPage("text=" + marker);
        assertThat(page.get("total").asLong()).isEqualTo(1);
        JsonNode summary = page.get("items").get(0);
        assertThat(summary.get("endpoint").asString()).isEqualTo("/echo");
        assertThat(summary.get("respStatus").asInt()).isEqualTo(200);

        HttpResponse<String> response = send("GET", ApiPaths.JOURNAL + "/" + summary.get("id").asString(), null, null);
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode details = json.readTree(response.body());
        assertThat(details.get("reqBody").asString()).isEqualTo(marker);
        assertThat(details.get("respBody").asString()).isEqualTo(marker);
        // заголовки отдаются объектом, а не строкой с JSON внутри
        assertThat(details.get("reqHeaders").isObject()).isTrue();
        assertThat(details.get("calls").isArray()).isTrue();
    }

    @Test
    void interfaceAndSoapAreNotJournaled() throws Exception {
        long before = count();

        assertThat(send("GET", ApiPaths.JOURNAL, null, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", ApiPaths.JOURNAL + "/stats", null, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", ApiPaths.INFO, null, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", ApiPaths.UI + "/", null, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", ApiPaths.UI + "/app.js", null, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/", null, null).statusCode()).isEqualTo(302);
        assertThat(send("POST", "/services", "text/xml", """
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body><echoRequest xmlns="http://cqa.ru/ws/echo"><message>x</message></echoRequest></soap:Body>
                </soap:Envelope>""").statusCode()).isEqualTo(200);

        assertThat(count()).isEqualTo(before);
    }

    @Test
    void infoDescribesApplication() throws Exception {
        JsonNode info = json.readTree(send("GET", ApiPaths.INFO, null, null).body());
        assertThat(info.get("journalReady").asBoolean()).isTrue();
        assertThat(info.get("saveAndMergePath").asString()).isEqualTo("/" + ApiPaths.SAVE_AND_MERGE);
        assertThat(info.get("mdm").get("scope").asString()).isEqualTo("DEV");
    }

    @Test
    void unknownAndMalformedIdsAreClientErrors() throws Exception {
        assertThat(send("GET", ApiPaths.JOURNAL + "/" + UUID.randomUUID(), null, null).statusCode()).isEqualTo(404);
        assertThat(send("GET", ApiPaths.JOURNAL + "/not-a-uuid", null, null).statusCode()).isEqualTo(400);
    }

    private long count() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM journal_request", Long.class);
    }

    /** Ответ пишется в журнал сразу после отправки клиенту — ждём, пока запись появится. */
    private JsonNode awaitPage(String query) throws Exception {
        for (int attempt = 0; attempt < 50; attempt++) {
            JsonNode page = json.readTree(send("GET", ApiPaths.JOURNAL + "?" + query, null, null).body());
            if (page.get("total").asLong() > 0 && !page.get("items").get(0).get("durationMs").isNull()) {
                return page;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("В журнале нет записи по запросу " + query);
    }

    private HttpResponse<String> send(String method, String path, String contentType, String body) throws Exception {
        String encoded = path.replace(" ", "%20");
        int q = encoded.indexOf('?');
        if (q >= 0) {
            encoded = encoded.substring(0, q + 1)
                    + java.net.URLEncoder.encode(encoded.substring(q + 1), StandardCharsets.UTF_8).replace("%3D", "=");
        }
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + encoded));
        if (contentType != null) {
            request.header("Content-Type", contentType);
        }
        request.method(method, body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}
