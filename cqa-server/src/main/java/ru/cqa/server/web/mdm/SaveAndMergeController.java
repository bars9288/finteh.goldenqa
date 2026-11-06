package ru.cqa.server.web.mdm;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.cqa.api.validation.SaveAndMergeValidatorException;
import ru.cqa.api.validation.Validator;
import ru.cqa.model.mdm.SaveAndMergeDTO;
import ru.cqa.server.db.sqlite.journal.JournalWriter;
import ru.cqa.server.mdm.MdmClient;
import ru.cqa.server.web.ApiPaths;
import ru.cqa.server.web.journal.JournalFilter;
import tools.jackson.databind.json.JsonMapper;

/**
 * Приём запроса saveAndMerge по REST: проверка и, если ошибок нет, пересылка в МДМ как прозрачный прокси.
 */
@Tag(name = "saveAndMerge", description = "Сохранение и слияние участников")
@RestController
@RequestMapping(path = ApiPaths.SAVE_AND_MERGE, produces = MediaType.APPLICATION_JSON_VALUE)
public class SaveAndMergeController {

    private final Validator<SaveAndMergeDTO> validator;
    private final MdmClient mdmClient;
    private final JsonMapper jsonMapper;
    private final JournalWriter journalWriter;

    /**
     * {@code @Lazy}: клиент МДМ создаётся при первом запросе, см. ExternalServicesConfiguration.
     */
    public SaveAndMergeController(Validator<SaveAndMergeDTO> validator, @Lazy MdmClient mdmClient, JsonMapper jsonMapper,
                                  JournalWriter journalWriter) {
        this.validator = validator;
        this.mdmClient = mdmClient;
        this.jsonMapper = jsonMapper;
        this.journalWriter = journalWriter;
    }

    /**
     * @param journalId id записи журнала от {@link JournalFilter}; null — запрос не журналируется
     */
    @Operation(summary = "Проверить запрос saveAndMerge и переслать его в МДМ")
    @PostMapping
    public ResponseEntity<byte[]> saveAndMerge(
            @RequestHeader HttpHeaders headers,
            @RequestBody byte[] requestBody,
            @RequestAttribute(name = JournalFilter.REQUEST_ID, required = false) UUID journalId) {
        SaveAndMergeDTO request = jsonMapper.readValue(requestBody, SaveAndMergeDTO.class);
        try {
            validator.validateBody(request);
        } catch (SaveAndMergeValidatorException exception) {
            journalWriter.validated(journalId, exception.getErrors());
            throw exception;
        }
        journalWriter.validated(journalId, List.of());
        return mdmClient.saveAndMerge(journalId, headers, requestBody);
    }

}
