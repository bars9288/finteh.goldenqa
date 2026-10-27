package ru.cqa.server.config;

import java.util.Optional;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Шапка OpenAPI-описания и ручное описание SOAP-транспорта.
 *
 * <p>REST-ручки springdoc находит сам, сканируя контроллеры. SOAP он не видит
 * и увидеть не может: сканируются только @RestController, а контракт SOAP —
 * это WSDL, другой формат.
 *
 * <p>Но SOAP поверх HTTP — это обычный POST с XML в теле. Поэтому адрес
 * {@code /services} описан здесь вручную как приём text/xml: в Swagger UI и
 * Scalar появляется форма с готовыми примерами конвертов, и запрос можно
 * отправить прямо из браузера. Полноценной заменой WSDL это не является —
 * там типы и операции, здесь просто «отправить XML по адресу».
 *
 * <p>Версия не пишется строкой: её источник — корневой pom. При сборке
 * spring-boot-maven-plugin (цель {@code build-info}) кладёт её в
 * {@code META-INF/build-info.properties}, откуда Spring поднимает
 * {@link BuildProperties}.
 */
@Configuration
public class OpenApiConfiguration {

    /** Запуск без предварительной сборки Maven — build-info.properties может не быть. */
    private static final String VERSION_UNKNOWN = "dev";

    /** SOAP 1.1 принимает только text/xml; с application/xml будет глухая 500. */
    private static final String SOAP_CONTENT_TYPE = "text/xml";

    private static final String ENVELOPE_HELLO = """
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <helloRequest xmlns="http://cqa.ru/ws/greeting">
                  <name>Сергей</name>
                  <family>Борискин</family>
                </helloRequest>
              </soap:Body>
            </soap:Envelope>""";

    private static final String ENVELOPE_SERVICE = """
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <serviceHelloRequest xmlns="http://cqa.ru/ws/greeting">
                  <typeService>KAFKA-CLIENT</typeService>
                  <scope>DEV</scope>
                </serviceHelloRequest>
              </soap:Body>
            </soap:Envelope>""";

    private static final String ENVELOPE_ECHO = """
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <echoRequest xmlns="http://cqa.ru/ws/echo">
                  <message>эхо, проверка связи</message>
                </echoRequest>
              </soap:Body>
            </soap:Envelope>""";

    @Bean
    public OpenAPI cqaOpenApi(@Value("${spring.application.name}") String applicationName,
                              @Value("${spring.webservices.path}") String soapPath,
                              ObjectProvider<BuildProperties> buildProperties) {

        String version = Optional.ofNullable(buildProperties.getIfAvailable())
                .map(BuildProperties::getVersion)
                .orElse(VERSION_UNKNOWN);
        // имена WSDL — имена бинов Wsdl11Definition в WebServiceConfiguration
        String wsdlGreeting = soapPath + "/greeting.wsdl";
        String wsdlEcho = soapPath + "/echo.wsdl";

        return new OpenAPI()
                .info(new Info()
                        .title("Единый тестировщик — API")
                        .version(version)
                        .description("Сервис " + applicationName + ". Описание REST-ручек сформировано "
                                + "автоматически по контроллерам. Часть операций доступна также "
                                + "по SOAP; контракты: " + wsdlGreeting + " (приветствия), "
                                + wsdlEcho + " (эхо)."))
                .externalDocs(new ExternalDocumentation()
                        .description("WSDL — контракт SOAP-сервиса приветствий")
                        .url(wsdlGreeting))
                .path(soapPath, soapEndpoint(wsdlGreeting, wsdlEcho));
    }

    /**
     * Описание SOAP-адреса как приёма произвольного XML.
     *
     * <p>Операция выбирается не адресом, а корневым элементом внутри
     * {@code <soap:Body>} — поэтому все варианты лежат примерами в одном теле.
     */
    private PathItem soapEndpoint(String wsdlGreeting, String wsdlEcho) {
        Content requestContent = new Content().addMediaType(SOAP_CONTENT_TYPE, new MediaType()
                .schema(new StringSchema().description("SOAP-конверт целиком"))
                .addExamples("Приветствие оператору", example(ENVELOPE_HELLO,
                        "Корневой элемент helloRequest в пространстве http://cqa.ru/ws/greeting"))
                .addExamples("Приветствие сервису", example(ENVELOPE_SERVICE,
                        "Корневой элемент serviceHelloRequest, то же пространство имён"))
                .addExamples("Эхо", example(ENVELOPE_ECHO,
                        "Другое пространство имён: http://cqa.ru/ws/echo")));

        Content responseContent = new Content()
                .addMediaType(SOAP_CONTENT_TYPE, new MediaType().schema(new StringSchema()));

        Operation operation = new Operation()
                .addTagsItem("SOAP")
                .summary("Отправить SOAP-запрос")
                .description("Единый адрес всех SOAP-операций. Выберите пример конверта — "
                        + "операция определяется корневым элементом тела, а не адресом. "
                        + "Content-Type обязан быть text/xml: с application/xml вернётся 500 без пояснений. "
                        + "Полный контракт — в WSDL: " + wsdlGreeting + ", " + wsdlEcho + ".")
                .operationId("soapCall")
                .requestBody(new RequestBody().required(true).content(requestContent))
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse()
                                .description("SOAP-ответ")
                                .content(responseContent))
                        .addApiResponse("500", new ApiResponse()
                                .description("SOAP Fault — для SOAP 1.1 это штатный код ошибки. "
                                        + "Смысл несёт faultcode: Client означает ошибку вызывающего")
                                .content(responseContent)));

        return new PathItem().post(operation);
    }

    private Example example(String value, String summary) {
        return new Example().summary(summary).value(value);
    }
}
