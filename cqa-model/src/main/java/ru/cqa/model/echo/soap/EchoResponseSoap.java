package ru.cqa.model.echo.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Тело SOAP-ответа эха — элемент {@code echoResponse} из XSD.
 */
@XmlRootElement(name = "echoResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class EchoResponseSoap {

    private String message;

    public EchoResponseSoap() {
    }

    public EchoResponseSoap(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
