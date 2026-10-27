package ru.cqa.server.ws.greeting;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import ru.cqa.cases.greeting.GreetingRegistry;
import ru.cqa.cases.greeting.operator.Operator;
import ru.cqa.cases.greeting.operator.OperatorMapper;
import ru.cqa.dto.soap.OperatorSoapDTO;
import ru.cqa.server.web.greeting.GreetingOperatorController;

/**
 * SOAP-аналог {@link GreetingOperatorController}.
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
    public HelloResponseSoap hello(@RequestPayload OperatorSoapDTO request) {
        return new HelloResponseSoap(greetings.greet(OperatorMapper.from(request)));
    }
}
