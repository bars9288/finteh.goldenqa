package ru.cqa.server.ws.greeting;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Тело SOAP-запроса приветствия внешнему сервису — элемент
 * {@code serviceHelloRequest} из XSD.
 */
@XmlRootElement(name = "serviceHelloRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"typeService", "scope"})
public class ServiceHelloRequestSoap {

    private String typeService;
    private String scope;

    public String getTypeService() {
        return typeService;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
