package ru.cqa.cases.greeting;

import java.util.Objects;
import ru.cqa.api.greeting.GreetingFacade;
import ru.cqa.cases.greeting.externalservice.ExternalServiceMapper;
import ru.cqa.cases.greeting.operator.OperatorMapper;
import ru.cqa.model.greeting.ExternalServiceDTO;
import ru.cqa.model.greeting.OperatorDTO;

public final class DefaultGreetingFacade implements GreetingFacade {
    private final GreetingRegistry greetings;

    public DefaultGreetingFacade(GreetingRegistry greetings) {
        this.greetings = Objects.requireNonNull(greetings, "greetings");
    }

    @Override
    public String greetOperator(OperatorDTO operator) {
        return greetings.greet(OperatorMapper.from(operator));
    }

    @Override
    public String greetService(ExternalServiceDTO service) {
        return greetings.greet(ExternalServiceMapper.from(service));
    }
}
