package ru.cqa.server.web.saveAndMerge;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.cqa.cases.validation.SaveAndMergeValidationException;
import ru.cqa.cases.validation.SaveAndMergeValidator;
import ru.cqa.dto.SaveAndMergeDTO;
import ru.cqa.server.web.ApiPaths;

/**
 * Приём запроса saveAndMerge и его проверка.
 *
 * <p>{@code @Valid} на теле намеренно не стоит: тогда Spring отверг бы запрос
 * с ошибками формы до вызова метода, и ошибки правил клиент увидел бы только
 * вторым заходом. Всю проверку — форму и правила — делает
 * {@link SaveAndMergeValidator} и возвращает все ошибки одним списком.
 */
@Tag(name = "saveAndMerge", description = "Сохранение и слияние участников")
@RestController
@RequestMapping(path = ApiPaths.SAVE_AND_MERGE, produces = MediaType.APPLICATION_JSON_VALUE)
public class SaveAndMergeController {

    private final SaveAndMergeValidator validator;

    public SaveAndMergeController(SaveAndMergeValidator validator) {
        this.validator = validator;
    }

    @Operation(summary = "Проверить запрос saveAndMerge")
    @PostMapping
    public SaveAndMergeResponse saveAndMerge(@RequestBody SaveAndMergeDTO body) {
        validator.validate(body);
        return SaveAndMergeResponse.ok();
    }

    /** Действует только на этот контроллер, общий ApiExceptionHandler не трогает. */
    @ExceptionHandler(SaveAndMergeValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SaveAndMergeResponse onInvalid(SaveAndMergeValidationException exception) {
        return SaveAndMergeResponse.failed(exception.getErrors());
    }
}
