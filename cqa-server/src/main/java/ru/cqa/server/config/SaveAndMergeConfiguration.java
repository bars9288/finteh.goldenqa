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
import ru.cqa.cases.validation.SaveAndMergeValidator;
import ru.cqa.server.db.sqlite.journal.JournalWriter;
import ru.cqa.server.mdm.MdmClient;

@Configuration
public class SaveAndMergeConfiguration {

    @Bean
    public SaveAndMergeValidator saveAndMergeValidator(Validator validator) {
        return new SaveAndMergeValidator(validator, PhysicalRules1.TO_SAVE_PHYSICAL);
    }

    @Bean
    @Lazy
    public MdmClient mdmClient(Environment env, JournalWriter journal) {
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

        String baseUrl = env.getRequiredProperty(connection + "url");
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();

        return new MdmClient(restClient, baseUrl, env.getRequiredProperty(connection + "endpoints.save-and-merge"), journal);
    }
}
