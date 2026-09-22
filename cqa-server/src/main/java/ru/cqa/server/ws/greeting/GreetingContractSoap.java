package ru.cqa.server.ws.greeting;

/**
 * Координаты контракта SOAP-сервиса.
 *
 * <p>Одно место, где записано пространство имён: на него ссылаются модели,
 * эндпоинты и описание WSDL. В XSD оно продублировано — там это неизбежно.
 */
public final class GreetingContractSoap {

    /** Совпадает с targetNamespace в schema/greeting.xsd. */
    public static final String NAMESPACE = "http://cqa.ru/ws/greeting";

    private GreetingContractSoap() {
    }
}
