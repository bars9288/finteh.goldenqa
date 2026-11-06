package ru.cqa.model.greeting.soap;

/**
 * Пространства имён SOAP-контрактов.
 *
 * <p>Пакет {@code soap} общий для всех SOAP-DTO, а у разных сервисов свои
 * пространства имён. Поэтому namespace задаётся не на уровне пакета, а в
 * каждом классе — через {@code @XmlRootElement} и {@code @XmlType}.
 *
 * <p>Каждое значение совпадает с targetNamespace своей XSD — там оно
 * продублировано неизбежно.
 */
public final class SoapNamespaces {

    /** schema/greeting.xsd */
    public static final String GREETING = "http://cqa.ru/ws/greeting";

    private SoapNamespaces() {
    }
}
