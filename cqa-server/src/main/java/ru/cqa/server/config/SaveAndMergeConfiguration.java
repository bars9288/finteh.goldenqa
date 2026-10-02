package ru.cqa.server.config;

import java.net.http.HttpClient;
import java.time.Duration;

import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import ru.cqa.cases.validation.physical.PhysicalRules;
import ru.cqa.cases.validation.SaveAndMergeValidator;
import ru.cqa.server.mdm.MdmClient;

/**
 * Бины кейса saveAndMerge. Сам валидатор живёт в cqa-case без Spring —
 * здесь ему передаётся движок Bean Validation, который собрал Spring Boot.
 */
@Configuration
public class SaveAndMergeConfiguration {

    @Bean
    public SaveAndMergeValidator saveAndMergeValidator(Validator validator) {
        return new SaveAndMergeValidator(validator, PhysicalRules.TO_SAVE_PHYSICAL);
    }

    /**
     * Клиент МДМ на фиксированном контуре {@code cqa.save-and-merge.mdm.scope}:
     * адрес, таймауты и путь ручки ({@code endpoints.save-and-merge}) — из {@code cqa.connections.CDI.<scope>}. Учётные данные оттуда
     * не берутся: в МДМ уходит {@code Authorization} самого клиента (прозрачный прокси).
     *
     * <p>{@code @Lazy}: бин создаётся при первом запросе, а не при старте — без
     * настроенного МДМ (нет .env) приложение и остальные ручки продолжают работать.
     */
    @Bean
    @Lazy
    public MdmClient mdmClient(Environment env) {
        String scope = env.getRequiredProperty("cqa.save-and-merge.mdm.scope");
        String connection = "cqa.connections.CDI." + scope + ".";

        // прозрачный прокси: в МДМ не уходит ничего, чего не прислал клиент —
        // ни попытки перейти на HTTP/2 (Upgrade: h2c), ни своего Accept-Encoding
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(env.getRequiredProperty(connection + "connect-timeout", Duration.class))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(env.getRequiredProperty(connection + "read-timeout", Duration.class));
        requestFactory.enableCompression(false);

        RestClient restClient = RestClient.builder()
                .baseUrl(env.getRequiredProperty(connection + "url"))
                .requestFactory(requestFactory)
                .build();

        return new MdmClient(restClient, env.getRequiredProperty(connection + "endpoints.save-and-merge"));
    }
}
