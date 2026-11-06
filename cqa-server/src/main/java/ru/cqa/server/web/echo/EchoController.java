package ru.cqa.server.web.echo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.cqa.model.echo.EchoDetails;
import ru.cqa.server.web.ApiPaths;

/** Эхо-ручки: что получили, то и вернули. */
@Tag(name = "Эхо", description = "Возврат полученного запроса — проверка связи и разбора данных")
@RestController
@RequestMapping(ApiPaths.ECHO)
public class EchoController {

    @Operation(summary = "Вернуть тело запроса без изменений",
               description = "Content-Type ответа повторяет Content-Type запроса. Параметры адресной строки игнорируются: они не входят в тело.")
    @PostMapping
    public ResponseEntity<String> echo(
            @RequestBody(required = false) String body,
            @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType) {

        MediaType mediaType = contentType != null
                ? MediaType.parseMediaType(contentType)
                : MediaType.TEXT_PLAIN;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(body != null ? body : "");
    }

    /**
     * Диагностическое эхо: помимо тела показывает метод, путь и параметры
     * адресной строки — и сырой строкой, и разобранными по ключам.
     */
    @Operation(summary = "Вернуть полную картину запроса",
               description = "Тело, метод, путь и параметры адресной строки — отдельно от тела.")
    @PostMapping(path = "/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public EchoDetails details(
            @RequestBody(required = false) String body,
            @RequestParam Map<String, String> queryParams,
            HttpServletRequest request) {

        return new EchoDetails(
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                queryParams,
                request.getContentType(),
                body);
    }
}
