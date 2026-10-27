package ru.cqa.server.web.saveAndMerge;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import ru.cqa.businesslogic.constants.PartyType;
import ru.cqa.businesslogic.exceptions.SaveAndMergeValidationException;
import ru.cqa.businesslogic.validation.validators.SaveAndMergeValidator;
import ru.cqa.dto.PartyDTO;
import ru.cqa.dto.SaveAndMergeDTO;
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

    private final Map<PartyType, SaveAndMergeValidator> validators;
    private final MdmClient mdmClient;
    private final JsonMapper jsonMapper;
    private final JournalWriter journal;

    /**
     * {@code @Lazy}: клиент МДМ создаётся при первом запросе, см. ExternalServicesConfiguration.
     */
    public SaveAndMergeController(Map<PartyType, SaveAndMergeValidator> validators, @Lazy MdmClient mdmClient, JsonMapper jsonMapper,
                                  JournalWriter journal) {
        this.validators = validators;
        this.mdmClient = mdmClient;
        this.jsonMapper = jsonMapper;
        this.journal = journal;
    }

    /**
     * @param journalId id записи журнала от {@link JournalFilter}; null — запрос не журналируется
     */
    @Operation(summary = "Проверить запрос saveAndMerge и переслать его в МДМ")
    @PostMapping
    public ResponseEntity<byte[]> saveAndMerge(
            @RequestHeader HttpHeaders headers,
            @RequestBody byte[] raw,
            @RequestAttribute(name = JournalFilter.REQUEST_ID, required = false) UUID journalId) {
        SaveAndMergeDTO body = jsonMapper.readValue(raw, SaveAndMergeDTO.class);
        try {
            validatorFor(body).validate(body);
        } catch (SaveAndMergeValidationException e) {
            journal.validated(journalId, e.getErrors());
            throw e;
        }
        journal.validated(journalId, List.of());
        return mdmClient.saveAndMerge(journalId, headers, raw);
    }

    private SaveAndMergeValidator validatorFor(SaveAndMergeDTO body) {
        List<PartyDTO> parties = body.party();
        String type = parties == null || parties.isEmpty() || parties.get(0) == null ? null : parties.get(0).type();
        return PartyType.of(type)
                .map(validators::get)
                .orElseThrow(() -> new SaveAndMergeValidationException(
                        List.of("party[0].type: ожидается одно из " + Arrays.toString(PartyType.values()))));
    }
}
