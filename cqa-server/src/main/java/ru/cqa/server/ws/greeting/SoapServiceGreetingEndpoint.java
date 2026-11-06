package ru.cqa.server.ws.greeting;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import ru.cqa.api.greeting.GreetingFacade;
import ru.cqa.model.greeting.ExternalServiceDTO;
import ru.cqa.model.greeting.soap.ExternalServiceSoapDTO;
import ru.cqa.model.greeting.soap.GreetingContractSoap;
import ru.cqa.model.greeting.soap.ServiceHelloResponseSoap;
import ru.cqa.server.web.greeting.ServiceGreetingController;

/**
 * SOAP-аналог {@link ServiceGreetingController}.
 *
 * <p>Тот же шаблон: реестр выбирает реализацию по типу адресата, транспорт на
 * это никак не влияет.
 */
@Endpoint
public class SoapServiceGreetingEndpoint {

    private final GreetingFacade greetings;

    public SoapServiceGreetingEndpoint(GreetingFacade greetings) {
        this.greetings = greetings;
    }

    @PayloadRoot(namespace = GreetingContractSoap.NAMESPACE, localPart = "serviceHelloRequest")
    @ResponsePayload
    public ServiceHelloResponseSoap hello(@RequestPayload ExternalServiceSoapDTO request) {
        return new ServiceHelloResponseSoap(greetings.greetService(new ExternalServiceDTO(request.getTypeService(), request.getScope())));
    }
}
