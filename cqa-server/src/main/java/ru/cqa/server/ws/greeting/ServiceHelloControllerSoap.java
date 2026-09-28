package ru.cqa.server.ws.greeting;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import ru.cqa.business.greeting.GreetingRegistry;
import ru.cqa.dto.soap.ExternalServiceSoapDTO;

/**
 * SOAP-аналог {@link ru.cqa.server.web.greeting.ServiceHelloController}.
 *
 * <p>Тот же шаблон: реестр выбирает реализацию по типу адресата, транспорт на
 * это никак не влияет.
 */
@Endpoint
public class ServiceHelloControllerSoap {

    private final GreetingRegistry greetings;

    public ServiceHelloControllerSoap(GreetingRegistry greetings) {
        this.greetings = greetings;
    }

    @PayloadRoot(namespace = GreetingContractSoap.NAMESPACE, localPart = "serviceHelloRequest")
    @ResponsePayload
    public ServiceHelloResponseSoap hello(@RequestPayload ExternalServiceSoapDTO request) {
        return new ServiceHelloResponseSoap(greetings.greet(request.toExternalService()));
    }
}
