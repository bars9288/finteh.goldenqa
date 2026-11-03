package ru.cqa.server.web.saveAndMerge;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import ru.cqa.validators.mdm.SaveAndMergeValidatorException;
import ru.cqa.server.db.sqlite.journal.JournalWriter;
import ru.cqa.server.web.ErrorResponse;
import ru.cqa.server.web.journal.JournalFilter;

import java.util.List;
import java.util.UUID;

@RestControllerAdvice(assignableTypes = {SaveAndMergeController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
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
    public ErrorResponse onMdmUnavailable(RestClientException exception) {
        return ErrorResponse.failed(List.of("МДМ недоступна: " + exception.getMessage()));
    }

    /** Ошибки проверки запроса — 400 со списком ошибок; в журнал их уже записал контроллер. */
    @ExceptionHandler(SaveAndMergeValidatorException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse onInvalid(SaveAndMergeValidatorException exception) {
        return ErrorResponse.failed(exception.getErrors());
    }

    /**
     * Клиент МДМ не собрался: не заданы адрес или учётные данные контура (нет .env).
     * Это наша конфигурация, а не ошибка клиента — поэтому 500, а не 400 из ApiExceptionHandler.
     */
    @ExceptionHandler(BeanCreationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse onMdmNotConfigured(
            BeanCreationException exception,
            @RequestAttribute(name = JournalFilter.REQUEST_ID, required = false) UUID journalId) {
        String error = "МДМ не настроена: " + exception.getMostSpecificCause().getMessage();
        journal.internalError(journalId, error);
        return ErrorResponse.failed(List.of(error));
    }
}
