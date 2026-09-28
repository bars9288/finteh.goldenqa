package ru.cqa.server.web.greeting;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.cqa.business.greeting.GreetingRegistry;
import ru.cqa.business.greeting.operator.Operator;
import ru.cqa.dto.OperatorDTO;

/**
 * Дежурная проверка доступности приложения.
 *
 * <p>Имя и фамилию можно передать двумя способами — каждый метод принимает
 * данные только одним путём, без смешивания:
 * <ul>
 *   <li>GET /helloAkBars?name=Иван&amp;family=Иванов</li>
 *   <li>POST /helloAkBars с телом {"name": "Иван", "family": "Иванов"}</li>
 * </ul>
 *
 * <p>Контроллер намеренно тонкий: разобрать запрос, позвать реестр, собрать
 * ответ. Логика приветствия живёт в модуле cqa-business. Какая именно
 * реализация сработает, решает реестр по типу адресата.
 */
@Tag(name = "Дежурная проверка", description = "Ручка для проверки доступности приложения")
@RestController
@RequestMapping(path = "/helloAkBars", produces = MediaType.APPLICATION_JSON_VALUE)
public class HelloController {

    private final GreetingRegistry greetings;

    /** Зависимость через конструктор: поле final, подмена в тестах тривиальна. */
    public HelloController(GreetingRegistry greetings) {
        this.greetings = greetings;
    }

    @Operation(summary = "Приветствие, параметры в строке запроса")
    @GetMapping
    public HelloResponse hello(@RequestParam("name") String name,
                               @RequestParam("family") String family) {
        return respond(name, family);
    }

    /**
     * Тело обязательно и проверяется целиком до входа в метод: при пустом
     * или неполном теле Spring отвечает 400, сюда управление не доходит.
     */
    @Operation(summary = "Приветствие, параметры в теле запроса")
    @PostMapping
    public HelloResponse hello(@Valid @RequestBody OperatorDTO body) {
        return respond(body.toOperator());
    }

    private HelloResponse respond(String name, String family) {
        return respond(new Operator(name, family));
    }

    private HelloResponse respond(Operator operator) {
        return new HelloResponse(greetings.greet(operator));
    }
}
