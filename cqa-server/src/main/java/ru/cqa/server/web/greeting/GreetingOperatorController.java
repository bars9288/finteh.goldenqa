package ru.cqa.server.web.greeting;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.cqa.business.greeting.GreetingRegistry;
import ru.cqa.business.greeting.operator.Operator;
import ru.cqa.cases.validation.exceptions.GreetingValidationException;
import ru.cqa.cases.validation.validators.GreetingValidator;
import ru.cqa.dto.HelloResponseDTO;
import ru.cqa.dto.OperatorDTO;
import ru.cqa.server.web.saveAndMerge.SaveAndMergeResponse;
import tools.jackson.databind.json.JsonMapper;

import static ru.cqa.cases.validation.constants.ApiPaths.GREETING_OPERATOR;

@Tag(name = "Дежурная проверка", description = "Ручка для проверки доступности приложения")
@RestController
@RequestMapping(path = GREETING_OPERATOR, produces = MediaType.APPLICATION_JSON_VALUE)
public class GreetingOperatorController {

    private final GreetingRegistry greetings;
    private final GreetingValidator validator;
    private final JsonMapper jsonMapper;

    /** Зависимость через конструктор: поле final, подмена в тестах тривиальна. */
    public GreetingOperatorController(GreetingRegistry greetings, GreetingValidator validator, JsonMapper jsonMapper) {
        this.greetings = greetings;
        this.validator = validator;
        this.jsonMapper = jsonMapper;
    }

    @Operation(summary = "Приветствие, параметры в строке запроса")
    @GetMapping
    public HelloResponseDTO hello(@RequestParam("name") String name,
                                  @RequestParam("family") String family) {
        try {
            validator.validate(new OperatorDTO(name, family));
        } catch (GreetingValidationException e) {
            throw e;
        }
        return respond(name, family);
    }

    /**
     * Тело обязательно и проверяется целиком до входа в метод: при пустом
     * или неполном теле Spring отвечает 400, сюда управление не доходит.
     */
    @Operation(summary = "Приветствие, параметры в теле запроса")
    @PostMapping
    public HelloResponseDTO hello(@Valid @RequestBody OperatorDTO body) {
        try {
            validator.validate(body);
        } catch (GreetingValidationException e) {
            throw e;
        }
        return respond(body.toOperator());
    }

    private HelloResponseDTO respond(String name, String family) {
        return respond(new Operator(name, family));
    }

    private HelloResponseDTO respond(Operator operator) {
        return new HelloResponseDTO(greetings.greet(operator));
    }
}
