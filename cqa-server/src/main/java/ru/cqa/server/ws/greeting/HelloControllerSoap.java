package ru.cqa.server.ws.greeting;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import ru.cqa.business.greeting.GreetingRegistry;
import ru.cqa.business.greeting.Operator;

/**
 * SOAP-аналог {@link ru.cqa.server.web.greeting.HelloController}.
 *
 * <p>Java-логика не менялась вообще: тот же {@link GreetingRegistry}, та же
 * доменная модель {@link Operator}. Отличается только транспорт — вместо
 * разбора query-параметров и JSON приходит и уходит XML.
 */
@Endpoint
public class HelloControllerSoap {

    private final GreetingRegistry greetings;

    public HelloControllerSoap(GreetingRegistry greetings) {
        this.greetings = greetings;
    }

    @PayloadRoot(namespace = GreetingContractSoap.NAMESPACE, localPart = "helloRequest")
    @ResponsePayload
    public HelloResponseSoap hello(@RequestPayload HelloRequestSoap request) {
        Operator operator = new Operator(request.getName(), request.getFamily());
        return new HelloResponseSoap(greetings.greet(operator));
    }
}
