package ru.cqa.server.db.sqlite.journal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

/**
 * Готовит БД журнала при старте: каталог, файл БД и таблицы (скрипт db/schema.sql).
 *
 * <p>Делает это сам, а не через {@code spring.sql.init}: та при любой ошибке
 * (нет прав на каталог, файл повреждён или занят) обрывает запуск всего приложения.
 * Здесь ошибка только пишется в лог, а журнал переходит на резервный файл —
 * см. {@link JournalFallbackFile}. Ручки работают в любом случае.
 */
@Component
class JournalSchema {

    private static final Logger log = LoggerFactory.getLogger(JournalSchema.class);

    private static final String SQLITE_PREFIX = "jdbc:sqlite:";

    private final boolean ready;

    JournalSchema(DataSource dataSource, @Value("${spring.datasource.url}") String url) {
        this.ready = prepare(dataSource, url);
    }

    /** true — БД готова; false — писать в неё бесполезно, журнал идёт в резервный файл. */
    boolean ready() {
        return ready;
    }

    private static boolean prepare(DataSource dataSource, String url) {
        try {
            createParentDirectories(url);
            // SQLite создаёт файл БД сам при первом подключении — его делает уже скрипт
            new ResourceDatabasePopulator(new ClassPathResource("db/schema.sql")).execute(dataSource);
            log.info("БД журнала готова: {}", url);
            return true;
        } catch (Exception e) {
            log.error("БД журнала недоступна ({}): журнал пишется в резервный файл", url, e);
            return false;
        }
    }

    /**
     * SQLite создаёт файл БД, но не каталоги на пути к нему — их создаём сами.
     * Путь — часть адреса между {@code jdbc:sqlite:} и параметрами {@code ?...}.
     */
    private static void createParentDirectories(String url) throws IOException {
        if (!url.startsWith(SQLITE_PREFIX)) {
            return;
        }
        String path = url.substring(SQLITE_PREFIX.length());
        int params = path.indexOf('?');
        if (params >= 0) {
            path = path.substring(0, params);
        }
        // БД в памяти и адреса вида file:... разбирать не берёмся
        if (path.isEmpty() || path.startsWith(":memory:") || path.startsWith("file:")) {
            return;
        }
        Path parent = Path.of(path).toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}
