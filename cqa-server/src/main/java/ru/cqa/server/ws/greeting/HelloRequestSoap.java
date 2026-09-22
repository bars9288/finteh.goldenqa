package ru.cqa.server.ws.greeting;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Тело SOAP-запроса приветствия оператору — элемент {@code helloRequest} из XSD.
 *
 * <p>Здесь обычный класс, а не record: JAXB требует конструктор без аргументов
 * и сеттеры, с record он работать не умеет. Проверок нет намеренно — за
 * корректность по-прежнему отвечает доменная модель.
 */
@XmlRootElement(name = "helloRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"name", "family"})
public class HelloRequestSoap {

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
