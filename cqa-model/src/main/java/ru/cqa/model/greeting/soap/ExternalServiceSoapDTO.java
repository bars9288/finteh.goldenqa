package ru.cqa.model.greeting.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Тело SOAP-запроса приветствия внешнему сервису — элемент
 * {@code serviceHelloRequest} из schema/greeting.xsd.
 *
 * <p>Устроен так же, как {@link OperatorSoapDTO}: проверки — в доменном
 * {@code ExternalService}.
 */
@XmlRootElement(name = "serviceHelloRequest", namespace = SoapNamespaces.GREETING)
@XmlType(namespace = SoapNamespaces.GREETING, propOrder = {"typeService", "scope"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ExternalServiceSoapDTO {

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
