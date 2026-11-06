package ru.cqa.model.execution;

import java.util.List;
import java.util.Map;

/** HTTP-ответ, полученный при выполнении HttpMove. */
public record HttpResponseData(
        int statusCode,
        Map<String, List<String>> headers,
        Object body) {
}
