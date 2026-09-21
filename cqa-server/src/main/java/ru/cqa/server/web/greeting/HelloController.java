package ru.cqa.server.web.greeting;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.cqa.business.greeting.GreetingService;
import ru.cqa.business.greeting.Operator;

/**
 * Дежурная проверка доступности приложения.
 *
 * <p>Имя и фамилию можно передать двумя способами:
 * <ul>
 *   <li>GET /helloAkBars?name=Иван&amp;family=Иванов</li>
 *   <li>POST /helloAkBars с телом {"name": "Иван", "family": "Иванов"}</li>
 * </ul>
 *
 * <p>Контроллер намеренно тонкий: разобрать запрос, позвать сервис, собрать
 * ответ. Логика приветствия живёт в модуле cqa-business.
 */
@Tag(name = "Дежурная проверка", description = "Ручка для проверки доступности приложения")
@RestController
@RequestMapping(path = "/helloAkBars", produces = MediaType.APPLICATION_JSON_VALUE)
public class HelloController {

    private final GreetingService greetingService;

    /** Зависимость через конструктор: поле final, подмена в тестах тривиальна. */
    public HelloController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @Operation(summary = "Приветствие, параметры в строке запроса")
    @GetMapping
    public HelloResponse hello(@RequestParam("name") String name,
                               @RequestParam("family") String family) {
        return respond(name, family);
    }

    /**
     * Тело необязательно: если его нет или в нём заполнены не все поля,
     * недостающее берётся из параметров запроса.
     */
    @Operation(summary = "Приветствие, параметры в теле запроса",
               description = "Тело необязательно: недостающие поля берутся из параметров запроса")
    @PostMapping
    public HelloResponse hello(@RequestBody(required = false) HelloRequest body,
                               @RequestParam(name = "name", required = false) String name,
                               @RequestParam(name = "family", required = false) String family
    ) {
        if (body == null) {
            return respond(name, family);
        }
        return respond(body.nameOr(name), body.familyOr(family));
    }

    private HelloResponse respond(String name, String family) {
        Operator operator = new Operator(name, family);
        return new HelloResponse(greetingService.greet(operator));
    }
}
