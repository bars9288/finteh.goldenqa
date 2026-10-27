package ru.cqa.server.web;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.core.JacksonException;

/** Общий обработчик — последним, после обработчиков отдельных ручек (у них HIGHEST_PRECEDENCE). */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail onInvalidInput(IllegalArgumentException exception) {
        return badRequest(exception.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail onMissingParameter(MissingServletRequestParameterException exception) {
        return badRequest("Параметр '" + exception.getParameterName() + "' обязателен");
    }

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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail onUnreadableBody(HttpMessageNotReadableException exception) {
        return badRequest("Тело запроса отсутствует или не является корректным JSON");
    }

    @ExceptionHandler(JacksonException.class)
    public ProblemDetail onUnparsableBody(JacksonException exception) {
        return badRequest("Тело запроса отсутствует или не является корректным JSON");
    }

    private ProblemDetail badRequest(String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Некорректные данные запроса");
        return problem;
    }
}
