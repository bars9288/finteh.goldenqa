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
import ru.cqa.business.greeting.externalservice.ExternalService;
import ru.cqa.business.greeting.GreetingRegistry;
import ru.cqa.dto.ExternalServiceDTO;

/**
 * Второй адресат приветствия — внешний сервис.
 *
 * <p>Показывает, как приложение растёт по шаблону: этот контроллер добавлен,
 * а {@link HelloController}, реестр и существующая реализация не менялись.
 * Зависимость та же самая — {@link GreetingRegistry}; нужную реализацию он
 * выбирает по типу переданного объекта.
 *
 * <p>Как и у {@link HelloController}, данные приходят одним путём на метод:
 * GET — параметры строки запроса, POST — только тело.
 */
@Tag(name = "Дежурная проверка", description = "Ручка для провеи доступности приложения")
@RestController
@RequestMapping(path = "/helloService", produces = MediaType.APPLICATION_JSON_VALUE)
public class ServiceHelloController {

    private final GreetingRegistry greetings;

    public ServiceHelloController(GreetingRegistry greetings) {
        this.greetings = greetings;
    }

    @Operation(summary = "Приветствие внешнему сервису, параметры в строке запроса")
    @GetMapping
    public HelloResponse hello(@RequestParam("typeService") String typeService,
                               @RequestParam("scope") String scope) {
        return respond(new ExternalService(typeService, scope));
    }

    /**
     * Тело обязательно и проверяется целиком до входа в метод: при пустом
     * или неполном теле Spring отвечает 400, сюда управление не доходит.
     */
    @Operation(summary = "Приветствие внешнему сервису, параметры в теле запроса")
    @PostMapping
    public HelloResponse hello(@Valid @RequestBody ExternalServiceDTO body) {
        return respond(body.toExternalService());
    }

    private HelloResponse respond(ExternalService service) {
        return new HelloResponse(greetings.greet(service));
    }
}
