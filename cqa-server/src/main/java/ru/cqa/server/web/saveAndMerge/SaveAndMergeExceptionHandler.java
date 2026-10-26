package ru.cqa.server.web.saveAndMerge;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import ru.cqa.cases.validation.exceptions.SaveAndMergeValidationException;
import ru.cqa.server.db.sqlite.journal.JournalWriter;
import ru.cqa.server.web.journal.JournalFilter;

import java.util.List;
import java.util.UUID;

@RestControllerAdvice(assignableTypes = {SaveAndMergeController.class})
public class SaveAndMergeExceptionHandler {

    private final JournalWriter journal;

    public SaveAndMergeExceptionHandler(JournalWriter journal) {
        this.journal = journal;
    }

    /**
     * МДМ не ответила: нет соединения, таймаут.
     * В журнал ошибку уже записал MdmClient (journal_call.call_error).
     */
    @ExceptionHandler(RestClientException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public SaveAndMergeResponse onMdmUnavailable(RestClientException exception) {
        return SaveAndMergeResponse.failed(List.of("МДМ недоступна: " + exception.getMessage()));
    }

    /**
     * Действует только на этот контроллер, общий ApiExceptionHandler не трогает.
     */
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
    public SaveAndMergeResponse onMdmNotConfigured(
            BeanCreationException exception,
            @RequestAttribute(name = JournalFilter.REQUEST_ID, required = false) UUID journalId) {
        String error = "МДМ не настроена: " + exception.getMostSpecificCause().getMessage();
        journal.internalError(journalId, error);
        return SaveAndMergeResponse.failed(List.of(error));
    }
}
