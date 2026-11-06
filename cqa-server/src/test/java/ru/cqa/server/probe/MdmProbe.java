package ru.cqa.server.probe;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Ручная проверка связи с МДМ: один запрос, ответ целиком в консоль.
 * Spring не нужен — запускается через main.
 */
public class MdmProbe {

    // что вызываем; для GET тело = null
    private static final String METHOD = "GET";
    private static final String PATH = "/";
    private static final String BODY = null;
    private static final String CONTENT_TYPE = "application/json";

    /** Относительно рабочего каталога — запускать из корня проекта. */
    private static final Path DOT_ENV = Path.of(".env");

    public static void main(String[] args) throws Exception {
        String baseUrl = env("MDM_DEV_URL");
        String user = env("MDM_DEV_USER");
        String password = env("MDM_DEV_PASSWORD");

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        String credentials = Base64.getEncoder()
                .encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));

        HttpRequest.BodyPublisher body = BODY == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(BODY, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + PATH))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", CONTENT_TYPE)
                .header("Accept", "*/*")
                .method(METHOD, body)
                .build();

        System.out.println(">>> " + METHOD + " " + request.uri());

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        System.out.println("<<< HTTP " + response.statusCode());
        response.headers().map().forEach((name, values) ->
                System.out.println(name + ": " + String.join(", ", values)));
        System.out.println();
        System.out.println(response.body());
    }

    /** Переменная окружения важнее .env — так же, как в Spring. */
    private static String env(String name) throws IOException {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = dotEnv().get(name);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Не задана переменная " + name
                    + " ни в окружении, ни в " + DOT_ENV.toAbsolutePath());
        }
        return value;
    }

    /** Строки ИМЯ=значение; пустые и начинающиеся с # пропускаются. */
    private static Map<String, String> dotEnv() throws IOException {
        Map<String, String> values = new HashMap<>();
        if (!Files.exists(DOT_ENV)) {
            return values;
        }
        for (String line : Files.readAllLines(DOT_ENV, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            int separatorIndex = trimmed.indexOf('=');
            if (trimmed.isEmpty() || trimmed.startsWith("#") || separatorIndex < 0) {
                continue;
            }
            values.put(trimmed.substring(0, separatorIndex).trim(), trimmed.substring(separatorIndex + 1).trim());
        }
        return values;
    }
}
