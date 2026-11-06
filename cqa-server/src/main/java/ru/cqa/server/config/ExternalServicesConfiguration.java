package ru.cqa.server.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import ru.cqa.server.db.sqlite.journal.JournalWriter;
import ru.cqa.server.kafka.KafkaClient;
import ru.cqa.server.mdm.MdmClient;

/** Клиенты внешних систем: МДМ, Kafka. Подключения — в connections.yml. */
@Configuration
public class ExternalServicesConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalServicesConfiguration.class);

    /** Общий HTTP-клиент приложения; его пул соединений используют MdmClient и сценарные HTTP Move. */
    @Bean
    public HttpClient sharedHttpClient(Environment environment) {
        String environmentId = environment.getProperty("cqa.save-and-merge.mdm.scope", "DEV");
        String connectionPropertyPrefix = "cqa.connections.MDM." + environmentId + ".";
        Duration connectTimeout = environment.getProperty(
                connectionPropertyPrefix + "connect-timeout", Duration.class, Duration.ofSeconds(10));
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(connectTimeout)
                .build();
    }

    @Bean
    @Lazy
    public MdmClient mdmClient(Environment environment, JournalWriter journalWriter, HttpClient sharedHttpClient) {
        String environmentId = environment.getRequiredProperty("cqa.save-and-merge.mdm.scope");
        String connectionPropertyPrefix = "cqa.connections.MDM." + environmentId + ".";

        // прозрачный прокси: в МДМ не уходит ничего, чего не прислал клиент —
        // ни попытки перейти на HTTP/2 (Upgrade: h2c), ни своего Accept-Encoding
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(sharedHttpClient);
        requestFactory.setReadTimeout(environment.getRequiredProperty(connectionPropertyPrefix + "read-timeout", Duration.class));
        requestFactory.enableCompression(false);

        String baseUrl = environment.getRequiredProperty(connectionPropertyPrefix + "url");
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();

        LOGGER.info("Инициализация клиента ПРОКСИ на MDM успешно завершена");

        return new MdmClient(restClient, baseUrl, environment.getRequiredProperty(connectionPropertyPrefix + "endpoints.save-and-merge"), journalWriter);
    }

    /**
     * Заглушка: клиента Kafka пока нет, бин равен {@code null}.
     * Обязательной зависимостью его не получить — только через {@code ObjectProvider<KafkaClient>}.
     */
    @Bean
    public KafkaClient kafkaClient() {
        LOGGER.info("Клиент Kafka — заглушка, бин равен null");
        return null;
    }
}
