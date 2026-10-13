package ru.cqa.server.db.sqlite.journal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * БД журнала создать нельзя: на месте каталога для неё лежит обычный файл.
 * Приложение всё равно стартует, а журнал пишется в резервный файл.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.config.additional-location=file:../")
class JournalUnavailableTest {

    @TempDir
    static Path dir;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) throws IOException {
        Path notADirectory = Files.createFile(dir.resolve("not-a-directory"));
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + notADirectory.resolve("journal.db"));
        registry.add("cqa.journal.fallback-dir", () -> dir.resolve("fallback").toString());
    }

    @Autowired
    JournalSchema schema;

    @Autowired
    JournalWriter writer;

    @LocalServerPort
    int port;

    /** Сам факт, что тест запустился, — уже проверка: контекст поднялся без БД. */
    @Test
    void applicationStartsAndJournalGoesToFallbackFile() throws IOException {
        assertThat(schema.ready()).isFalse();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        UUID id = writer.requestReceived(Instant.parse("2026-10-12T11:30:00.123Z"), "127.0.0.1", 1,
                "POST", "/api/saveAndMerge", null, headers, "{\"party\":[]}".getBytes());
        writer.validated(id, List.of("party: ошибка"));

        // файл общий для всех тестов класса — берём только строки этой записи
        List<String> lines = fallbackLines().stream().filter(line -> line.contains(id.toString())).toList();
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0))
                .matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\+03:00 requestReceived \\{.*")
                .contains("\"receivedAt\":\"2026-10-12T14:30:00.123+03:00\"");
        assertThat(lines.get(1)).contains(" validated ").contains("\"validation_status\":\"FAILED\"");
    }

    /** Ручка отвечает как обычно, а запрос и ответ оказываются в резервном файле. */
    @Test
    void handlerAnswersAndHttpRequestGoesToFallbackFile() throws Exception {
        String tag = "t=" + UUID.randomUUID();
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/echo?" + tag))
                        .header("Content-Type", "text/plain")
                        .POST(HttpRequest.BodyPublishers.ofString("эхо"))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("эхо");

        // ответ пишется в журнал сразу после отправки клиенту — даём ему мгновение
        List<String> lines = List.of();
        for (int attempt = 0; attempt < 50 && lines.stream().noneMatch(l -> l.contains(" responseSent ")); attempt++) {
            Thread.sleep(100);
            lines = fallbackLines().stream().filter(l -> l.contains(tag) || l.contains(" responseSent ")).toList();
        }
        assertThat(lines).anySatisfy(line -> assertThat(line).contains(" requestReceived ").contains(tag));
        assertThat(lines).anySatisfy(line -> assertThat(line).contains(" responseSent ").contains("\"resp_status\":200"));
    }

    private static List<String> fallbackLines() throws IOException {
        try (Stream<Path> files = Files.list(dir.resolve("fallback"))) {
            List<String> lines = new java.util.ArrayList<>();
            for (Path file : files.toList()) {
                lines.addAll(Files.readAllLines(file));
            }
            return lines;
        }
    }
}
