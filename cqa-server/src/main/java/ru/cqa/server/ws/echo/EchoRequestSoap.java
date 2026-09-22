package ru.cqa.server.ws.echo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Тело SOAP-запроса эха — элемент {@code echoRequest} из XSD.
 */
@XmlRootElement(name = "echoRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class EchoRequestSoap {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
