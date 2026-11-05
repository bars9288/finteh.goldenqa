package ru.cqa.server.scenario;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import ru.cqa.scenario.definition.model.move.HttpMove;
import ru.cqa.scenario.runtime.execution.ExecutionContext;
import ru.cqa.scenario.runtime.executor.MoveExecutor;
import ru.cqa.scenario.runtime.result.HttpResponseData;
import ru.cqa.scenario.runtime.result.MoveResult;
import ru.cqa.scenario.runtime.result.MoveStatus;

/** Выполняет HTTP-запрос, описанный в HttpMove. */
@Component
public final class HttpMoveExecutor implements MoveExecutor<HttpMove> {
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);

    private final Environment environment;
    private final JsonMapper jsonMapper;
    private final HttpClient httpClient;

    public HttpMoveExecutor(Environment environment, JsonMapper jsonMapper, HttpClient httpClient) {
        this.environment = environment;
        this.jsonMapper = jsonMapper;
        this.httpClient = httpClient;
    }

    @Override
    public String getType() {
        return "http";
    }

    @Override
    public Class<HttpMove> getSupportedType() {
        return HttpMove.class;
    }

    @Override
    public MoveResult execute(HttpMove move, ExecutionContext context) {
        long startedAt = System.nanoTime();
        try {
            URI uri = resolveUri(move, context);
            Duration readTimeout = connectionProperty(
                    move, context, "read-timeout", Duration.class, DEFAULT_READ_TIMEOUT);

            JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
            requestFactory.setReadTimeout(readTimeout);
            RestClient client = RestClient.builder().requestFactory(requestFactory).build();

            HttpMethod method = HttpMethod.valueOf(move.method());
            RestClient.RequestBodySpec request = client.method(method).uri(uri);
            if (move.headers() != null) {
                request.headers(headers -> move.headers().forEach(headers::set));
            }
            if (move.requestBody() != null) {
                request.body(move.requestBody());
            }

            HttpResponseData response = request.exchange((httpRequest, httpResponse) -> {
                Map<String, List<String>> responseHeaders = new LinkedHashMap<>();
                httpResponse.getHeaders().forEach((name, values) -> responseHeaders.put(name, List.copyOf(values)));
                byte[] bytes = httpResponse.getBody().readAllBytes();
                Object body = parseBody(bytes);
                return new HttpResponseData(httpResponse.getStatusCode().value(), Map.copyOf(responseHeaders), body);
            });
/* десь SUCCESS означает: HTTP-обмен состоялся и ответ получен. Даже если код ответа — 4xx или 5xx, это пока не транспортная ошибка */
            return new MoveResult(move.id(), MoveStatus.SUCCESS, response, null, elapsedMs(startedAt));
        } catch (Exception exception) {
            String message = exception.getMessage() == null
                    ? exception.getClass().getSimpleName()
                    : exception.getMessage();
            return error(move, "Не удалось выполнить HTTP-запрос: " + message, startedAt);
        }
    }

    private URI resolveUri(HttpMove move, ExecutionContext context) {
        URI path = URI.create(move.path());
        if (path.isAbsolute()) {
            if (!"http".equalsIgnoreCase(path.getScheme()) && !"https".equalsIgnoreCase(path.getScheme())) {
                throw new IllegalArgumentException("Для HttpMove допустимы только схемы http и https");
            }
            return path;
        }

        String environmentId = context.environmentId();
        if (environmentId == null || environmentId.isBlank()) {
            throw new IllegalArgumentException("Для относительного path нужно указать environmentId запуска");
        }
        String property = connectionPropertyName(move, environmentId, "url");
        String baseUrl = environment.getRequiredProperty(property);
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String relativePath = move.path().startsWith("/") ? move.path().substring(1) : move.path();
        return UriComponentsBuilder.fromUriString(normalizedBase).path(relativePath).build().toUri();
    }

    private <T> T connectionProperty(HttpMove move, ExecutionContext context, String propertyName,
                                     Class<T> targetType, T defaultValue) {
        String environmentId = context.environmentId();
        if (environmentId == null || environmentId.isBlank()) {
            return defaultValue;
        }
        return environment.getProperty(connectionPropertyName(move, environmentId, propertyName), targetType, defaultValue);
    }

    private static String connectionPropertyName(HttpMove move, String environmentId, String propertyName) {
        return "cqa.connections." + move.connectionId() + "." + environmentId + "." + propertyName;
    }

    private Object parseBody(byte[] bytes) {
        if (bytes.length == 0) {
            return null;
        }
        String body = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        try {
            return jsonMapper.readValue(body, Object.class);
        } catch (JacksonException ignored) {
            return body;
        }
    }

    private static MoveResult error(HttpMove move, String message, long startedAt) {
        return new MoveResult(move.id(), MoveStatus.ERROR, null, message, elapsedMs(startedAt));
    }

    private static long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
