package ru.cqa.server.web.scenario;

import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.core.JacksonException;
import ru.cqa.scenario.creation.ScenarioAlreadyExistsException;
import ru.cqa.scenario.definition.validation.ScenarioValidatorException;
import ru.cqa.scenario.runtime.execution.ScenarioNotFoundException;
import ru.cqa.server.scenario.ScenarioStorageException;
import ru.cqa.server.web.ErrorResponse;

@RestControllerAdvice(assignableTypes = {ScenarioController.class, ScenarioExecutionController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ScenarioExceptionHandler {

    @ExceptionHandler(ScenarioNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse onScenarioNotFound(ScenarioNotFoundException exception) {
        return ErrorResponse.failed(List.of(exception.getMessage()));
    }

    @ExceptionHandler({JacksonException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse onInvalidJson(RuntimeException exception) {
        return ErrorResponse.failed(List.of(exception.getMessage()));
    }

    @ExceptionHandler(ScenarioValidatorException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse onInvalidScenario(ScenarioValidatorException exception) {
        return ErrorResponse.failed(exception.getErrors());
    }

    @ExceptionHandler(ScenarioAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse onAlreadyExists(ScenarioAlreadyExistsException exception) {
        return ErrorResponse.failed(List.of(exception.getMessage()));
    }

    @ExceptionHandler(ScenarioStorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse onStorageFailure(ScenarioStorageException exception) {
        return ErrorResponse.failed(List.of("Не удалось сохранить или прочитать сценарий"));
    }
}
