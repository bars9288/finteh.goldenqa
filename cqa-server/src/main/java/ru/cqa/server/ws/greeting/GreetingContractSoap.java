package ru.cqa.server.ws.greeting;

import ru.cqa.dto.soap.SoapNamespaces;

/**
 * Координаты контракта SOAP-сервиса.
 *
 * <p>Само значение живёт в {@link SoapNamespaces} модуля cqa-dto — рядом с
 * SOAP-DTO, которые его используют. Здесь только ссылка для эндпоинтов,
 * ответов и описания WSDL.
 */
public final class GreetingContractSoap {

    /** Совпадает с targetNamespace в schema/greeting.xsd. */
    public static final String NAMESPACE = SoapNamespaces.GREETING;

    private GreetingContractSoap() {
    }
}
