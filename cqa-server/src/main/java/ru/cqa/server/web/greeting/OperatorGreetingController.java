package ru.cqa.server.web.greeting;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.cqa.api.greeting.GreetingFacade;
import ru.cqa.api.validation.Validator;
import ru.cqa.model.greeting.HelloResponseDTO;
import ru.cqa.model.greeting.OperatorDTO;

import static ru.cqa.server.web.ApiPaths.GREETING_OPERATOR;

@Tag(name = "Дежурная проверка", description = "Ручка для проверки доступности приложения")
@RestController
@RequestMapping(path = GREETING_OPERATOR, produces = MediaType.APPLICATION_JSON_VALUE)
public class OperatorGreetingController {

    private final GreetingFacade greetings;
    private final Validator<OperatorDTO> validator;

    /** Зависимость через конструктор: поле final, подмена в тестах тривиальна. */
    public OperatorGreetingController(GreetingFacade greetings, Validator<OperatorDTO> validator) {
        this.greetings = greetings;
        this.validator = validator;
    }

    @Operation(summary = "Приветствие, параметры в строке запроса")
    @GetMapping
    public HelloResponseDTO hello(@RequestParam("name") String name,
                                  @RequestParam("family") String family) {
        OperatorDTO operator = new OperatorDTO(name, family);
        validator.validateBody(operator);
        return respond(operator);
    }

    /**
     * {@code @Valid} на теле намеренно не стоит: форму и правила проверяет {@link Validator}
     * и возвращает все ошибки одним списком.
     */
    @Operation(summary = "Приветствие, параметры в теле запроса")
    @PostMapping
    public HelloResponseDTO hello(@RequestBody OperatorDTO body) {
        validator.validateBody(body);
        return respond(body);
    }

    private HelloResponseDTO respond(OperatorDTO operator) {
        return new HelloResponseDTO(greetings.greetOperator(operator));
    }
}
