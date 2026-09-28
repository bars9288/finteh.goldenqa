package ru.cqa.server.web;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Единая точка превращения ошибок в HTTP-ответ.
 *
 * <p>Домен сообщает о некорректных данных через {@link IllegalArgumentException};
 * здесь это становится ответом 400, а не 500. Контроллеры при этом остаются
 * чистыми — без try/catch.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail onInvalidInput(IllegalArgumentException exception) {
        return badRequest(exception.getMessage());
    }

    /**
     * Параметр запроса вообще не передан. Без этого обработчика Spring вернул бы
     * ответ другого формата — клиенту пришлось бы разбирать две разные схемы ошибки.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail onMissingParameter(MissingServletRequestParameterException exception) {
        return badRequest("Параметр '" + exception.getParameterName() + "' обязателен");
    }

    /**
     * Тело запроса не прошло Bean Validation ({@code @Valid}). Ошибки собраны
     * по всем полям сразу, чтобы клиент исправил запрос за один заход.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail onInvalidBody(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new TreeMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problem = badRequest("Тело запроса не прошло проверку");
        problem.setProperty("errors", errors);
        return problem;
    }

    /** Тела нет вовсе или это не JSON — до валидации дело не дошло. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail onUnreadableBody(HttpMessageNotReadableException exception) {
        return badRequest("Тело запроса отсутствует или не является корректным JSON");
    }

    private ProblemDetail badRequest(String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Некорректные данные запроса");
        return problem;
    }
}
