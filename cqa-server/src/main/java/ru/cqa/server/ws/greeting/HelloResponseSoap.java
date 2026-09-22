package ru.cqa.server.ws.greeting;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Тело SOAP-ответа — элемент {@code helloResponse} из XSD.
 */
@XmlRootElement(name = "helloResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class HelloResponseSoap {

    private String message;

    public HelloResponseSoap() {
    }

    public HelloResponseSoap(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
