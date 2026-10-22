package ru.cqa.business.greeting.externalservice;

import ru.cqa.business.greeting.GreetingService;

public class ExternalServiceGreetingService implements GreetingService<ExternalService> {

    private static final String TEMPLATE = "Привет из приложения \"Единый тестировщик\" сервису ";

    @Override
    public Class<ExternalService> subjectType() {
        return ExternalService.class;
    }

    @Override
    public String greet(ExternalService service) {
        if (service == null) {
            throw new IllegalArgumentException("Сервис не задан");
        }
        return TEMPLATE + service.typeService() + " на контуре " + service.scope();
    }
}
