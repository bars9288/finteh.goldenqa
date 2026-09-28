/**
 * SOAP-DTO: тела запросов, которые приходят в SOAP-конверте.
 *
 * <p>На уровне пакета задана только квалификация элементов, но не namespace:
 * сервисы с разными пространствами имён живут в одном пакете, поэтому
 * namespace каждый класс объявляет сам (см. {@link ru.cqa.dto.soap.SoapNamespaces}).
 * Вложенные элементы получают namespace своего класса из {@code @XmlType}.
 */
@XmlSchema(elementFormDefault = XmlNsForm.QUALIFIED)
package ru.cqa.dto.soap;

import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;
