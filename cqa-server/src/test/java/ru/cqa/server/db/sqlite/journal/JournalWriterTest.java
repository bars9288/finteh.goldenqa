package ru.cqa.server.db.sqlite.journal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Запись в журнал на настоящей SQLite — во временном файле, своём на каждый запуск.
 *
 * <p>application.yml лежит в корне репозитория, а тест запускается из каталога модуля —
 * поэтому корень подключён явно. Из его настроек подменяется только адрес БД.
 */
@SpringBootTest(properties = "spring.config.additional-location=file:../")
class JournalWriterTest {

    @TempDir
    static Path dir;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> "jdbc:sqlite:" + dir.resolve("journal.db") + "?foreign_keys=true&journal_mode=WAL&busy_timeout=5000");
        registry.add("cqa.journal.fallback-dir", () -> dir.resolve("fallback").toString());
    }

    @Autowired
    JournalWriter writer;

    @Autowired
    JournalRequestRepository requests;

    @Autowired
    JournalCallRepository calls;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void fullCycleOfProxiedRequest() {
        Instant received = Instant.parse("2026-10-12T11:30:00.123Z");
        HttpHeaders in = headers("application/json");
        in.add("X-Multi", "a");
        in.add("X-Multi", "b");

        UUID id = writer.requestReceived(received, "10.0.0.5", 51234, "POST", "/api/saveAndMerge", "x=1",
                in, utf8("{\"party\":[]}"));
        writer.validated(id, List.of());
        UUID callId = writer.callStarted(id, "MDM", received.plusMillis(10), "http://mdm/saveAndMerge", in);
        writer.callAnswered(callId, received.plusMillis(200), 200, headers("application/json"), utf8("{\"ok\":true}"));
        writer.responseSent(id, received.plusMillis(210), 200, headers("application/json"), utf8("{\"ok\":true}"), null);

        JournalRequest request = requests.findById(id).orElseThrow();
        assertThat(request.receivedAt()).isEqualTo(received);
        assertThat(request.reqBodyKind()).isEqualTo(BodyKind.JSON);
        assertThat(request.reqBody()).isEqualTo("{\"party\":[]}");
        assertThat(request.reqHeaders()).contains("\"X-Multi\":[\"a\",\"b\"]");
        assertThat(request.validationStatus()).isEqualTo(ValidationStatus.PASSED);
        assertThat(request.validationErrors()).isNull();
        assertThat(request.respStatus()).isEqualTo(200);
        assertThat(request.delivered()).isTrue();

        assertThat(calls.findByRequestIdOrderBySentAt(id))
                .singleElement()
                .satisfies(call -> {
                    assertThat(call.respStatus()).isEqualTo(200);
                    assertThat(call.respBodyKind()).isEqualTo(BodyKind.JSON);
                    assertThat(call.callError()).isNull();
                });

        // в самой БД — текст по Москве фиксированной ширины
        assertThat(jdbc.queryForObject("SELECT received_at FROM journal_request WHERE id = ?", String.class, id.toString()))
                .isEqualTo("2026-10-12T14:30:00.123+03:00");
    }

    @Test
    void failedValidationAndUndeliveredResponse() {
        UUID id = received(headers("application/json"), utf8("{\"party\":"));
        writer.validated(id, List.of("party: не должно быть пустым", "party[0].type: обязательно"));
        writer.responseSent(id, Instant.now(), 400, headers("application/json"), utf8("{}"), "Broken pipe");

        JournalRequest request = requests.findById(id).orElseThrow();
        assertThat(request.reqBodyKind()).isEqualTo(BodyKind.TEXT);
        assertThat(request.validationStatus()).isEqualTo(ValidationStatus.FAILED);
        assertThat(request.validationErrors()).isEqualTo("[\"party: не должно быть пустым\",\"party[0].type: обязательно\"]");
        assertThat(request.delivered()).isFalse();
        assertThat(request.deliveryError()).isEqualTo("Broken pipe");
    }

    @Test
    void bodyKinds() {
        assertThat(kindOf(headers(null), null)).isEqualTo(BodyKind.EMPTY);
        assertThat(kindOf(headers("text/xml"), utf8("  <soap:Envelope/>"))).isEqualTo(BodyKind.XML);
        assertThat(kindOf(headers("text/plain"), utf8("{\"json\":\"с неверным Content-Type\"}"))).isEqualTo(BodyKind.JSON);
        assertThat(kindOf(headers("кривой/тип;;"), utf8("просто строка"))).isEqualTo(BodyKind.TEXT);

        UUID binary = received(headers("application/octet-stream"), new byte[] {(byte) 0xFF, (byte) 0xFE, 0x00});
        JournalRequest request = requests.findById(binary).orElseThrow();
        assertThat(request.reqBodyKind()).isEqualTo(BodyKind.BINARY);
        assertThat(request.reqBody()).isEqualTo("//4A");
    }

    @Test
    void failedCallAndInternalError() {
        UUID id = received(headers("application/json"), utf8("{}"));
        UUID callId = writer.callStarted(id, "MDM", Instant.now(), "http://mdm/saveAndMerge", headers(null));
        writer.callFailed(callId, Instant.now(), "Read timed out");
        writer.internalError(id, "МДМ недоступна");

        assertThat(calls.findById(callId).orElseThrow().callError()).isEqualTo("Read timed out");
        assertThat(requests.findById(id).orElseThrow().internalError()).isEqualTo("МДМ недоступна");
    }

    @Test
    void rejectedByDatabaseGoesToFallbackFile() throws IOException {
        UUID unknown = UUID.randomUUID();
        writer.validated(unknown, List.of());
        // внешний ключ включён в URL соединения — вызов без запроса в БД не запишется
        UUID orphanCall = writer.callStarted(UUID.randomUUID(), "MDM", Instant.now(), "http://mdm", headers(null));

        assertThat(calls.findById(orphanCall)).isEmpty();
        assertThat(fallbackLines())
                .anySatisfy(line -> assertThat(line).contains(" validated ").contains(unknown.toString()))
                .anySatisfy(line -> assertThat(line).contains(" callStarted ").contains(orphanCall.toString()));
    }

    private static List<String> fallbackLines() throws IOException {
        try (Stream<Path> files = Files.list(dir.resolve("fallback"))) {
            List<String> lines = new ArrayList<>();
            for (Path file : files.toList()) {
                assertThat(file.getFileName().toString()).matches("\\d{4}-\\d{2}-\\d{2}-journal\\.log");
                lines.addAll(Files.readAllLines(file));
            }
            return lines;
        }
    }

    private UUID received(HttpHeaders headers, byte[] body) {
        return writer.requestReceived(Instant.now(), "127.0.0.1", 1, "POST", "/api/test", null, headers, body);
    }

    private BodyKind kindOf(HttpHeaders headers, byte[] body) {
        return requests.findById(received(headers, body)).orElseThrow().reqBodyKind();
    }

    private static HttpHeaders headers(String contentType) {
        HttpHeaders headers = new HttpHeaders();
        if (contentType != null) {
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);
        }
        return headers;
    }

    private static byte[] utf8(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }
}
