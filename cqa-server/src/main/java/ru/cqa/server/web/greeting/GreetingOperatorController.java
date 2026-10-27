package ru.cqa.server.web.greeting;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.cqa.cases.greeting.GreetingRegistry;
import ru.cqa.cases.greeting.operator.Operator;
import ru.cqa.cases.greeting.operator.OperatorMapper;
import ru.cqa.business.validation.validators.GreetingValidator;
import ru.cqa.dto.HelloResponseDTO;
import ru.cqa.dto.OperatorDTO;

import static ru.cqa.server.web.ApiPaths.GREETING_OPERATOR;

@Tag(name = "Дежурная проверка", description = "Ручка для проверки доступности приложения")
@RestController
@RequestMapping(path = GREETING_OPERATOR, produces = MediaType.APPLICATION_JSON_VALUE)
public class GreetingOperatorController {

    private final GreetingRegistry greetings;
    private final GreetingValidator validator;

    /** Зависимость через конструктор: поле final, подмена в тестах тривиальна. */
    public GreetingOperatorController(GreetingRegistry greetings, GreetingValidator validator) {
        this.greetings = greetings;
        this.validator = validator;
    }

    @Operation(summary = "Приветствие, параметры в строке запроса")
    @GetMapping
    public HelloResponseDTO hello(@RequestParam("name") String name,
                                  @RequestParam("family") String family) {
        OperatorDTO operator = new OperatorDTO(name, family);
        validator.validate(operator);
        return respond(OperatorMapper.from(operator));
    }

    /**
     * {@code @Valid} на теле намеренно не стоит: форму и правила проверяет {@link GreetingValidator}
     * и возвращает все ошибки одним списком.
     */
    @Operation(summary = "Приветствие, параметры в теле запроса")
    @PostMapping
    public HelloResponseDTO hello(@RequestBody OperatorDTO body) {
        validator.validate(body);
        return respond(OperatorMapper.from(body));
    }

    private HelloResponseDTO respond(Operator operator) {
        return new HelloResponseDTO(greetings.greet(operator));
    }
}
