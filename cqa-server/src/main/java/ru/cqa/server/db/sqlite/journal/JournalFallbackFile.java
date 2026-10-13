package ru.cqa.server.db.sqlite.journal;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.cqa.server.db.sqlite.SqliteConverters;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Резервный журнал: текстовый файл на случай, когда БД недоступна или запись в неё упала.
 *
 * <p>Файл на каждый день, дата — префиксом имени: {@code logs/2026-10-12-journal.log}.
 * Одна строка — одна операция журнала: время по Москве, операция, данные в JSON:
 * <pre>
 * 2026-10-12T14:30:00.123+03:00 requestReceived {"id":"...","endpoint":"/api/saveAndMerge",...}
 * </pre>
 *
 * <p>Никогда не бросает исключений: если не удалось записать и сюда, запись
 * уходит в основной лог приложения с уровнем ERROR.
 */
@Component
class JournalFallbackFile {

    private static final Logger log = LoggerFactory.getLogger(JournalFallbackFile.class);

    private static final DateTimeFormatter DAY =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withZone(SqliteConverters.MOSCOW);

    private final Path dir;
    private final JsonMapper jsonMapper;

    JournalFallbackFile(@Value("${cqa.journal.fallback-dir:logs}") Path dir, JsonMapper jsonMapper) {
        this.dir = dir;
        // копия общего JsonMapper, где время — по Москве, как в БД, а не в UTC (…Z) по умолчанию
        this.jsonMapper = jsonMapper.rebuild()
                .addModule(new SimpleModule().addSerializer(Instant.class, new MoscowInstantSerializer()))
                .build();
    }

    /** synchronized: строки из параллельных запросов не перемешиваются внутри файла. */
    synchronized void write(String operation, Object data) {
        Instant now = Instant.now();
        try {
            String line = SqliteConverters.format(now) + " " + operation + " "
                    + jsonMapper.writeValueAsString(data) + System.lineSeparator();
            Files.createDirectories(dir);
            Files.writeString(dir.resolve(DAY.format(now) + "-journal.log"), line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            log.error("Запись журнала потеряна — не удалось записать и в резервный файл: {} {}", operation, data, e);
        }
    }

    /** {@code 2026-10-12T14:30:00.123+03:00} — тот же формат, что в БД. */
    private static class MoscowInstantSerializer extends StdSerializer<Instant> {

        MoscowInstantSerializer() {
            super(Instant.class);
        }

        @Override
        public void serialize(Instant value, JsonGenerator generator, SerializationContext context) {
            generator.writeString(SqliteConverters.format(value));
        }
    }
}
