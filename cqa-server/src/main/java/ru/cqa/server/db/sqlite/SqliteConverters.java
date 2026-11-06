package ru.cqa.server.db.sqlite;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

/**
 * Преобразования типов Java в типы SQLite и обратно для Spring Data JDBC.
 *
 * <p>Своих типов для времени, UUID и BOOLEAN у SQLite нет: время и UUID хранятся
 * текстом, флаг — числом 0/1. Без этих конвертеров Spring Data передал бы драйверу
 * {@code Instant} или {@code UUID} как есть (sqlite-jdbc их не понимает), а 0/1
 * не смог бы прочитать в {@code Boolean}.
 */
public final class SqliteConverters {

    /** Время в БД — по Москве, пояс указан явно в самом значении. */
    public static final ZoneId MOSCOW_ZONE = ZoneId.of("Europe/Moscow");

    /**
     * Фиксированная ширина: всегда три знака миллисекунд и смещение {@code +03:00}.
     * Поэтому сортировка строк совпадает с сортировкой по времени — на этом
     * держится индекс по received_at. {@code uuuu} — год без эры, см. DateTimeFormatter.
     */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSxxx").withZone(MOSCOW_ZONE);

    private SqliteConverters() {
    }

    /** Время текстом, как оно хранится в БД: {@code 2026-10-12T14:30:00.123+03:00}. */
    public static String format(Instant instant) {
        return TIMESTAMP_FORMATTER.format(instant);
    }

    public static List<Converter<?, ?>> all() {
        return List.of(new InstantToTextConverter(), new TextToInstantConverter(), new UuidToTextConverter(), new TextToUuidConverter(),
                new IntegerToBooleanConverter());
    }

    /** {@code 2026-10-12T14:30:00.123+03:00} */
    @WritingConverter
    static class InstantToTextConverter implements Converter<Instant, String> {
        @Override
        public String convert(Instant source) {
            return format(source);
        }
    }

    @ReadingConverter
    static class TextToInstantConverter implements Converter<String, Instant> {
        @Override
        public Instant convert(String source) {
            return OffsetDateTime.parse(source).toInstant();
        }
    }

    @WritingConverter
    static class UuidToTextConverter implements Converter<UUID, String> {
        @Override
        public String convert(UUID source) {
            return source.toString();
        }
    }

    @ReadingConverter
    static class TextToUuidConverter implements Converter<String, UUID> {
        @Override
        public UUID convert(String source) {
            return UUID.fromString(source);
        }
    }

    /** Запись не нужна: {@code Boolean} драйвер сам кладёт как 1/0, а читает обратно как {@code Integer}. */
    @ReadingConverter
    static class IntegerToBooleanConverter implements Converter<Integer, Boolean> {
        @Override
        public Boolean convert(Integer source) {
            return source != 0;
        }
    }
}
