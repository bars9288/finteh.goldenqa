package ru.cqa.model.greeting.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Тело SOAP-ответа — элемент {@code serviceHelloResponse} из XSD.
 */
@XmlRootElement(namespace = SoapNamespaces.GREETING, name = "serviceHelloResponse")
@jakarta.xml.bind.annotation.XmlType(namespace = SoapNamespaces.GREETING)
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceHelloResponseSoap {

    private String message;

    public ServiceHelloResponseSoap() {
    }

    public ServiceHelloResponseSoap(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
