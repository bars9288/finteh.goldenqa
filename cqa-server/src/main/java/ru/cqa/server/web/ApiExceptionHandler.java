package ru.cqa.server.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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

    private ProblemDetail badRequest(String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Некорректные данные запроса");
        return problem;
    }
}
