package ru.cqa.server.web.greeting;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.cqa.cases.validation.exceptions.GreetingValidationException;
import ru.cqa.server.web.saveAndMerge.SaveAndMergeResponse;

@RestControllerAdvice(assignableTypes = {GreetingOperatorController.class, GreetingServiceController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GreetingExceptionHandler {

    /**
     * Действует только на этот контроллер, общий ApiExceptionHandler не трогает.
     */
    @ExceptionHandler(GreetingValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SaveAndMergeResponse onInvalid(GreetingValidationException exception) {
        return SaveAndMergeResponse.failed(exception.getErrors());
    }
}
