package ru.cqa.server.web.greeting;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.cqa.business.greeting.externalservice.ExternalService;
import ru.cqa.business.greeting.GreetingRegistry;

/**
 * Второй адресат приветствия — внешний сервис.
 *
 * <p>Показывает, как приложение растёт по шаблону: этот контроллер добавлен,
 * а {@link HelloController}, реестр и существующая реализация не менялись.
 * Зависимость та же самая — {@link GreetingRegistry}; нужную реализацию он
 * выбирает по типу переданного объекта.
 */
@Tag(name = "Дежурная проверка", description = "Ручка для проверки доступности приложения")
@RestController
@RequestMapping(path = "/helloService", produces = MediaType.APPLICATION_JSON_VALUE)
public class ServiceHelloController {

    private final GreetingRegistry greetings;

    public ServiceHelloController(GreetingRegistry greetings) {
        this.greetings = greetings;
    }

    @Operation(summary = "Приветствие внешнему сервису")
    @GetMapping
    public HelloResponse hello(@RequestParam("typeService") String typeService,
                               @RequestParam("scope") String scope) {
        ExternalService service = new ExternalService(typeService, scope);
        return new HelloResponse(greetings.greet(service));
    }
}
