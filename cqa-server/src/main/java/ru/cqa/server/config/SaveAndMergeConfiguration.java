package ru.cqa.server.config;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import ru.cqa.businesslogic.constants.PartyType;
import ru.cqa.businesslogic.validation.rules.party.legal.LegalPartySaveRules;
import ru.cqa.businesslogic.validation.rules.party.physical.PhysicalPartySaveRules;
import ru.cqa.businesslogic.validation.validators.SaveAndMergeValidator;
import ru.cqa.server.db.sqlite.journal.JournalWriter;
import ru.cqa.server.mdm.MdmClient;

@Configuration
public class SaveAndMergeConfiguration {

    Logger logger = LoggerFactory.getLogger(SaveAndMergeConfiguration.class);

    @Bean
    public Map<PartyType, SaveAndMergeValidator> saveAndMergeValidators(jakarta.validation.Validator validator) {
        logger.info("Инициализация правил и валидации ../saveAndMerge");
        return Map.of(
                PHYSICAL, new SaveAndMergeValidator(validator, new PhysicalPartySaveRules().ruleValues()),
                LEGAL, new SaveAndMergeValidator(validator, new LegalPartySaveRules().ruleValues()));
    }

    @Bean
    @Lazy
    public MdmClient mdmClient(Environment env, JournalWriter journal) {
        String scope = env.getRequiredProperty("cqa.save-and-merge.mdm.scope");
        String connection = "cqa.connections.MDM." + scope + ".";

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

        logger.info("Инициализация клиента ПРОКСИ на MDM успешно завершена");

        return new MdmClient(restClient, baseUrl, env.getRequiredProperty(connection + "endpoints.save-and-merge"), journal);
    }
}
