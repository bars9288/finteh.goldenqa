package ru.cqa.server.web.saveAndMerge;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import ru.cqa.cases.validation.SaveAndMergeValidationException;
import ru.cqa.cases.validation.SaveAndMergeValidator;
import ru.cqa.dto.SaveAndMergeDTO;
import ru.cqa.server.mdm.MdmClient;
import ru.cqa.server.web.ApiPaths;
import tools.jackson.databind.json.JsonMapper;

/**
 * Приём запроса saveAndMerge: проверка и, если ошибок нет, пересылка в МДМ как прозрачный прокси.
 *
 * <p>Тело принимается сырыми байтами: в МДМ уходит ровно то, что прислал клиент,
 * а не JSON, заново собранный из DTO (поля вне DTO, порядок и формат чисел
 * сохраняются), вместе с заголовками клиента — см. {@link MdmClient}.
 * Для проверки те же байты разбираются в {@link SaveAndMergeDTO}.
 *
 * <p>{@code @Valid} на теле намеренно не стоит: всю проверку — форму и правила —
 * делает {@link SaveAndMergeValidator} и возвращает все ошибки одним списком.
 */
@Tag(name = "saveAndMerge", description = "Сохранение и слияние участников")
@RestController
@RequestMapping(path = ApiPaths.SAVE_AND_MERGE, produces = MediaType.APPLICATION_JSON_VALUE)
public class SaveAndMergeController {

    private final SaveAndMergeValidator validator;
    private final MdmClient mdmClient;
    private final JsonMapper jsonMapper;

    /** {@code @Lazy}: клиент МДМ создаётся при первом запросе, см. SaveAndMergeConfiguration. */
    public SaveAndMergeController(SaveAndMergeValidator validator, @Lazy MdmClient mdmClient, JsonMapper jsonMapper) {
        this.validator = validator;
        this.mdmClient = mdmClient;
        this.jsonMapper = jsonMapper;
    }

    @Operation(summary = "Проверить запрос saveAndMerge и переслать его в МДМ")
    @PostMapping
    public ResponseEntity<byte[]> saveAndMerge(@RequestHeader HttpHeaders headers, @RequestBody byte[] raw) {
        SaveAndMergeDTO body = jsonMapper.readValue(raw, SaveAndMergeDTO.class);
        validator.validate(body);
        return mdmClient.saveAndMerge(headers, raw);
    }

    /** Действует только на этот контроллер, общий ApiExceptionHandler не трогает. */
    @ExceptionHandler(SaveAndMergeValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SaveAndMergeResponse onInvalid(SaveAndMergeValidationException exception) {
        return SaveAndMergeResponse.failed(exception.getErrors());
    }

    /**
     * Клиент МДМ не собрался: не заданы адрес или учётные данные контура (нет .env).
     * Это наша конфигурация, а не ошибка клиента — поэтому 500, а не 400 из ApiExceptionHandler.
     */
    @ExceptionHandler(BeanCreationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public SaveAndMergeResponse onMdmNotConfigured(BeanCreationException exception) {
        return SaveAndMergeResponse.failed(List.of("МДМ не настроена: " + exception.getMostSpecificCause().getMessage()));
    }

    /** МДМ не ответила: нет соединения, таймаут. Её ответы с 4xx/5xx сюда не попадают — они пересылаются как есть. */
    @ExceptionHandler(RestClientException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public SaveAndMergeResponse onMdmUnavailable(RestClientException exception) {
        return SaveAndMergeResponse.failed(List.of("МДМ недоступна: " + exception.getMessage()));
    }
}
