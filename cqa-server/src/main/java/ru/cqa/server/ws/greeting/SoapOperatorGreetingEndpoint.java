package ru.cqa.server.ws.greeting;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import ru.cqa.api.greeting.GreetingFacade;
import ru.cqa.model.greeting.OperatorDTO;
import ru.cqa.model.greeting.soap.GreetingContractSoap;
import ru.cqa.model.greeting.soap.HelloResponseSoap;
import ru.cqa.model.greeting.soap.OperatorSoapDTO;

/** SOAP-приветствие: доменное преобразование выполняет реализация GreetingFacade. */
@Endpoint
public class SoapOperatorGreetingEndpoint {

    private final GreetingFacade greetings;

    public SoapOperatorGreetingEndpoint(GreetingFacade greetings) {
        this.greetings = greetings;
    }

    @PayloadRoot(namespace = GreetingContractSoap.NAMESPACE, localPart = "helloRequest")
    @ResponsePayload
    public HelloResponseSoap hello(@RequestPayload OperatorSoapDTO request) {
        return new HelloResponseSoap(greetings.greetOperator(new OperatorDTO(request.getName(), request.getFamily())));
    }
}
