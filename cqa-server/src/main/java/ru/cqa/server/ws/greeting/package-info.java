/**
 * SOAP-слой приветствий.
 *
 * <p>Пространство имён объявлено на уровне пакета: так его не приходится
 * повторять в каждом классе, и оно не задевает REST-модели из соседнего
 * пакета {@code web.greeting}.
 */
@XmlSchema(
        namespace = GreetingContractSoap.NAMESPACE,
        elementFormDefault = XmlNsForm.QUALIFIED)
package ru.cqa.server.ws.greeting;

import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;
