package ru.cqa.server.web.greeting;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.cqa.cases.mdm.exceptions.GreetingValidationException;
import ru.cqa.server.web.ErrorResponse;

@RestControllerAdvice(assignableTypes = {GreetingOperatorController.class, GreetingServiceController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GreetingExceptionHandler {

    /** Ошибки проверки оператора — 400 со списком ошибок. */
    @ExceptionHandler(GreetingValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse onInvalid(GreetingValidationException exception) {
        return ErrorResponse.failed(exception.getErrors());
    }
}
