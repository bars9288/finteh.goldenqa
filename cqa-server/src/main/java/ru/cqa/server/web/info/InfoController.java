package ru.cqa.server.web.info;

import java.time.Instant;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.cqa.server.db.sqlite.SqliteConverters;
import ru.cqa.server.db.sqlite.journal.JournalReader;
import ru.cqa.server.web.ApiPaths;

/**
 * Сводка о самом приложении для веб-интерфейса: версия, контур МДМ, состояние журнала.
 *
 * <p>Секретов не отдаёт: адрес МДМ — да (он нужен, чтобы понять, куда уйдёт запрос),
 * логин и пароль — нет.
 */
@Tag(name = "Служебное", description = "Состояние приложения")
@RestController
@RequestMapping(path = ApiPaths.INFO, produces = MediaType.APPLICATION_JSON_VALUE)
public class InfoController {

    /** Запуск без предварительной сборки Maven — build-info.properties может не быть. */
    private static final String VERSION_UNKNOWN = "dev";

    private final Environment env;
    private final ObjectProvider<BuildProperties> buildProperties;
    private final JournalReader journal;

    public InfoController(Environment env, ObjectProvider<BuildProperties> buildProperties, JournalReader journal) {
        this.env = env;
        this.buildProperties = buildProperties;
        this.journal = journal;
    }

    @Operation(summary = "Версия, контур МДМ, состояние журнала")
    @GetMapping
    public Info info() {
        BuildProperties build = buildProperties.getIfAvailable();
        String scope = env.getProperty("cqa.save-and-merge.mdm.scope");
        String prefix = "cqa.connections.CDI." + scope + ".";
        return new Info(
                env.getProperty("spring.application.name"),
                Optional.ofNullable(build).map(BuildProperties::getVersion).orElse(VERSION_UNKNOWN),
                Optional.ofNullable(build).map(BuildProperties::getTime).map(SqliteConverters::format).orElse(null),
                SqliteConverters.format(Instant.now()),
                "/" + ApiPaths.SAVE_AND_MERGE,
                new Mdm(scope, resolved(prefix + "type"), resolved(prefix + "url"),
                        resolved(prefix + "endpoints.save-and-merge")),
                journal.ready());
    }

    /**
     * Значение свойства или null, если его нет или в нём неразрешённая переменная
     * окружения — например, {@code ${CDI_DEV_URL}} без .env.
     */
    private String resolved(String key) {
        try {
            String value = env.getProperty(key);
            return value == null || value.isBlank() ? null : value;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public record Info(
            @Schema(description = "Имя приложения") String application,
            @Schema(description = "Версия из pom; dev — запуск без сборки Maven") String version,
            @Schema(description = "Время сборки") String buildTime,
            @Schema(description = "Время сервера") String serverTime,
            @Schema(description = "Адрес ручки saveAndMerge") String saveAndMergePath,
            Mdm mdm,
            @Schema(description = "Журнал пишется в БД; false — в резервный файл") boolean journalReady) {
    }

    /**
     * @param url null — адрес не задан (нет .env или переменной CDI_&lt;контур&gt;_URL),
     *            запрос saveAndMerge после проверки упадёт с 500
     */
    public record Mdm(String scope, String type, String url, String saveAndMergePath) {
    }
}
