package ru.cqa.bootstrap;

import java.io.StringWriter;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import jakarta.xml.bind.JAXBContext;
import org.junit.jupiter.api.Test;
import ru.cqa.model.echo.soap.EchoResponseSoap;
import ru.cqa.model.greeting.soap.HelloResponseSoap;
import ru.cqa.model.greeting.soap.ServiceHelloResponseSoap;

/** Перенос JAXB-классов между пакетами не должен менять XML-контракт. */
class SoapContractTest {
    @Test
    void movedResponsesStillMatchPublishedSchemas() throws Exception {
        var responses = Map.of(
                new HelloResponseSoap("Иван Петров"), "/schema/greeting.xsd",
                new ServiceHelloResponseSoap("MDM DEV"), "/schema/greeting.xsd",
                new EchoResponseSoap("эхо"), "/schema/echo.xsd");
        for (var entry : responses.entrySet()) {
            var schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    .newSchema(getClass().getResource(entry.getValue()));
            var marshaller = JAXBContext.newInstance(entry.getKey().getClass()).createMarshaller();
            marshaller.setSchema(schema);
            marshaller.marshal(entry.getKey(), new StringWriter());
        }
    }
}
