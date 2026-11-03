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
import ru.cqa.cases.grerting.GreetingRegistry;
import ru.cqa.cases.grerting.externalservice.ExternalService;
import ru.cqa.cases.grerting.externalservice.ExternalServiceMapper;
import ru.cqa.dto.ExternalServiceDTO;
import ru.cqa.dto.HelloResponseDTO;

import static ru.cqa.server.web.ApiPaths.GREETING_SERVICE;

@Tag(name = "Дежурная проверка", description = "Ручка для проверки доступности приложения")
@RestController
@RequestMapping(path = GREETING_SERVICE, produces = MediaType.APPLICATION_JSON_VALUE)
public class GreetingServiceController {

    private final GreetingRegistry greetings;

    public GreetingServiceController(GreetingRegistry greetings) {
        this.greetings = greetings;
    }

    @Operation(summary = "Приветствие внешнему сервису, параметры в строке запроса")
    @GetMapping
    public HelloResponseDTO hello(@RequestParam("typeService") String typeService,
                                  @RequestParam("scope") String scope) {
        return respond(new ExternalService(typeService, scope));
    }


    @Operation(summary = "Приветствие внешнему сервису, параметры в теле запроса")
    @PostMapping
    public HelloResponseDTO hello(@Valid @RequestBody ExternalServiceDTO body) {
        return respond(ExternalServiceMapper.from(body));
    }

    private HelloResponseDTO respond(ExternalService service) {
        return new HelloResponseDTO(greetings.greet(service));
    }
}
