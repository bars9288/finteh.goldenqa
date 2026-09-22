package ru.cqa.server.config;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import ru.cqa.server.ws.echo.EchoContractSoap;
import ru.cqa.server.ws.greeting.GreetingContractSoap;

/**
 * Настройка SOAP-слоя.
 *
 * <p>Подход contract-first: истина — в {@code schema/greeting.xsd}, WSDL
 * генерируется из неё на лету. Отдельно поддерживать WSDL-файл не нужно.
 *
 * <p>Аннотации @EnableWs здесь намеренно нет: она создаёт бин
 * WsConfigurationSupport, а автоконфигурация Spring Boot при его наличии
 * отключается целиком — вместе с регистрацией SOAP-сервлета, и WSDL
 * перестаёт отдаваться. Автоконфигурация даёт ровно то же самое.
 *
 * <p>Сервлет SOAP поднимает автоконфигурация Spring Boot по пути
 * {@code spring.webservices.path}; WSDL доступен по имени бина —
 * {@code <путь>/greeting.wsdl}.
 */
@Configuration
public class WebServiceConfiguration {

    @Bean
    public XsdSchema greetingSchema() {
        return new SimpleXsdSchema(new ClassPathResource("schema/greeting.xsd"));
    }

    /** Имя бина становится именем WSDL: greeting → /services/greeting.wsdl */
    @Bean(name = "greeting")
    public Wsdl11Definition greetingWsdl(XsdSchema greetingSchema) {
        DefaultWsdl11Definition definition = new DefaultWsdl11Definition();
        definition.setPortTypeName("GreetingPort");
        definition.setLocationUri("/services");
        definition.setTargetNamespace(GreetingContractSoap.NAMESPACE);
        definition.setSchema(greetingSchema);
        return definition;
    }

    @Bean
    public XsdSchema echoSchema() {
        return new SimpleXsdSchema(new ClassPathResource("schema/echo.xsd"));
    }

    /** echo → /services/echo.wsdl */
    @Bean(name = "echo")
    public Wsdl11Definition echoWsdl(XsdSchema echoSchema) {
        DefaultWsdl11Definition definition = new DefaultWsdl11Definition();
        definition.setPortTypeName("EchoPort");
        definition.setLocationUri("/services");
        definition.setTargetNamespace(EchoContractSoap.NAMESPACE);
        definition.setSchema(echoSchema);
        return definition;
    }

    /**
     * Домен сообщает о некорректных данных через {@link IllegalArgumentException}.
     * В REST это 400, здесь — SOAP Fault с кодом Client, то есть «виноват вызывающий».
     * Без этого любая такая ошибка выглядела бы как сбой сервера.
     */
    @Bean
    public SoapFaultMappingExceptionResolver soapFaultExceptionResolver() {
        SoapFaultMappingExceptionResolver resolver = new SoapFaultMappingExceptionResolver();

        SoapFaultDefinition clientFault = new SoapFaultDefinition();
        clientFault.setFaultCode(SoapFaultDefinition.CLIENT);
        resolver.setDefaultFault(clientFault);

        Properties mappings = new Properties();
        mappings.setProperty(IllegalArgumentException.class.getName(), SoapFaultDefinition.CLIENT.toString());
        resolver.setExceptionMappings(mappings);
        resolver.setOrder(1);
        return resolver;
    }
}
