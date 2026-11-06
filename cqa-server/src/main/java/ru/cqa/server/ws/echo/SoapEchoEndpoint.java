package ru.cqa.server.ws.echo;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import ru.cqa.model.echo.soap.EchoContractSoap;
import ru.cqa.model.echo.soap.EchoRequestSoap;
import ru.cqa.model.echo.soap.EchoResponseSoap;

/**
 * SOAP-аналог {@link ru.cqa.server.web.echo.EchoController}.
 *
 * <p>У SOAP нет ни адресной строки, ни разных путей на операцию: все запросы
 * идут POST'ом на один адрес, а операция определяется корневым элементом тела.
 * Поэтому диагностического варианта, как в REST, здесь нет — показывать нечего.
 */
@Endpoint
public class SoapEchoEndpoint {

    @PayloadRoot(namespace = EchoContractSoap.NAMESPACE, localPart = "echoRequest")
    @ResponsePayload
    public EchoResponseSoap echo(@RequestPayload EchoRequestSoap request) {
        return new EchoResponseSoap(request.getMessage());
    }
}
