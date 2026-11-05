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

    private static final Logger logger = LoggerFactory.getLogger(ExternalServicesConfiguration.class);

    /** Общий HTTP-клиент приложения; его пул соединений используют MdmClient и сценарные HTTP Move. */
    @Bean
    public HttpClient sharedHttpClient(Environment env) {
        String scope = env.getProperty("cqa.save-and-merge.mdm.scope", "DEV");
        String connection = "cqa.connections.MDM." + scope + ".";
        Duration connectTimeout = env.getProperty(
                connection + "connect-timeout", Duration.class, Duration.ofSeconds(10));
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(connectTimeout)
                .build();
    }

    @Bean
    @Lazy
    public MdmClient mdmClient(Environment env, JournalWriter journal, HttpClient sharedHttpClient) {
        String scope = env.getRequiredProperty("cqa.save-and-merge.mdm.scope");
        String connection = "cqa.connections.MDM." + scope + ".";

        // прозрачный прокси: в МДМ не уходит ничего, чего не прислал клиент —
        // ни попытки перейти на HTTP/2 (Upgrade: h2c), ни своего Accept-Encoding
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(sharedHttpClient);
        requestFactory.setReadTimeout(env.getRequiredProperty(connection + "read-timeout", Duration.class));
        requestFactory.enableCompression(false);

        String baseUrl = env.getRequiredProperty(connection + "url");
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();

        logger.info("Инициализация клиента ПРОКСИ на MDM успешно завершена");

        return new MdmClient(restClient, baseUrl, env.getRequiredProperty(connection + "endpoints.save-and-merge"), journal);
    }

    /**
     * Заглушка: клиента Kafka пока нет, бин равен {@code null}.
     * Обязательной зависимостью его не получить — только через {@code ObjectProvider<KafkaClient>}.
     */
    @Bean
    public KafkaClient kafkaClient() {
        logger.info("Клиент Kafka — заглушка, бин равен null");
        return null;
    }
}
