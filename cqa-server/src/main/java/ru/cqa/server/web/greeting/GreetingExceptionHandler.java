package ru.cqa.server.web.greeting;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.cqa.api.validation.GreetingValidatorException;
import ru.cqa.model.web.ErrorResponse;

@RestControllerAdvice(assignableTypes = {OperatorGreetingController.class, ServiceGreetingController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GreetingExceptionHandler {

    /** Ошибки проверки оператора — 400 со списком ошибок. */
    @ExceptionHandler(GreetingValidatorException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse onInvalid(GreetingValidatorException exception) {
        return ErrorResponse.failed(exception.getErrors());
    }
}
