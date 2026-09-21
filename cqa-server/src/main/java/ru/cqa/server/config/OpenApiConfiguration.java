package ru.cqa.server.config;

import java.util.Optional;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Шапка OpenAPI-описания: то, что видно вверху страницы Swagger UI.
 *
 * <p>Сами ручки springdoc находит сам, сканируя контроллеры, — описывать их
 * здесь не нужно. Здесь только общие сведения о сервисе.
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

    @Bean
    public OpenAPI cqaOpenApi(@Value("${spring.application.name}") String applicationName,
                              ObjectProvider<BuildProperties> buildProperties) {

        String version = Optional.ofNullable(buildProperties.getIfAvailable())
                .map(BuildProperties::getVersion)
                .orElse(VERSION_UNKNOWN);

        return new OpenAPI().info(new Info()
                .title("Единый тестировщик — API")
                .version(version)
                .description("Сервис " + applicationName + ". Описание сформировано автоматически по контроллерам."));
    }
}
