package ru.cqa.model.greeting.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Тело SOAP-запроса приветствия оператору — элемент {@code helloRequest}
 * из schema/greeting.xsd.
 *
 * <p>Обычный класс, а не record: JAXB требует конструктор без аргументов
 * и изменяемые поля. Аннотаций Bean Validation здесь нет намеренно —
 * Spring-WS их не запускает. Пустые поля отвергнет доменный {@code Operator},
 * клиент получит SOAP Fault с кодом Client.
 */
@XmlRootElement(name = "helloRequest", namespace = SoapNamespaces.GREETING)
@XmlType(namespace = SoapNamespaces.GREETING, propOrder = {"name", "family"})
@XmlAccessorType(XmlAccessType.FIELD)
public class OperatorSoapDTO {

    private String name;
    private String family;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }
}
