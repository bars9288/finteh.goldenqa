/**
 * SOAP-слой эха.
 *
 * <p>Своё пространство имён, объявленное на уровне пакета, — как и у
 * приветствий в соседнем пакете {@code ws.greeting}.
 */
@XmlSchema(
        namespace = EchoContractSoap.NAMESPACE,
        elementFormDefault = XmlNsForm.QUALIFIED)
package ru.cqa.model.echo.soap;

import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;
