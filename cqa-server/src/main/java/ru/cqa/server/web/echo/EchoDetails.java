package ru.cqa.server.web.echo;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Полная картина пришедшего запроса.
 *
 * <p>Нужна, чтобы увидеть то, чего не видно в чистом эхе: метод, адрес и —
 * главное — параметры адресной строки, которые в тело запроса не попадают.
 */
public record EchoDetails(

        @Schema(description = "HTTP-метод", example = "POST")
        String method,

        @Schema(description = "Путь без строки параметров", example = "/echo/details")
        String uri,

        @Schema(description = "Строка параметров как есть", example = "a=1&b=2")
        String queryString,

        @Schema(description = "Разобранные параметры адресной строки")
        Map<String, String> queryParams,

        @Schema(description = "Content-Type запроса", example = "application/json")
        String contentType,

        @Schema(description = "Тело запроса как есть")
        String body) {
}
