package ru.cqa.api.greeting;

import ru.cqa.model.greeting.ExternalServiceDTO;
import ru.cqa.model.greeting.OperatorDTO;

/** Приветствия без зависимости вызывающего кода от доменных классов. */
public interface GreetingFacade {
    String greetOperator(OperatorDTO operator);
    String greetService(ExternalServiceDTO service);
}
