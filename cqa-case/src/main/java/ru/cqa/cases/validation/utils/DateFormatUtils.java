package ru.cqa.cases.validation.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

public final class DateFormatUtils {

    public static final List<String> MOSCOW_ZONES = List.of("МСК", "MSK", "+03:00", "Europe/Moscow");

    public static final DateTimeFormatter DD_MM_YYYY_STRICT = DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter DD_MM_YYYY_HH_MM_SS_STRICT = DateTimeFormatter.ofPattern("dd.MM.uuuu HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);

    private DateFormatUtils() {
    }

    public static List<String> checkDateTimeZnMsk(String[] parts, String fieldPath, String formatError) {
        String path = fieldPath + ": ";
        if ((parts.length != 2 && parts.length != 3) || !isDdMmYyyyHhMmSs(parts[0] + " " + parts[1])) {
            return List.of(path + formatError);
        }
        if (parts.length == 3 && !MOSCOW_ZONES.contains(parts[2])) {
            return List.of(path + "часовой пояс должен быть московский: " + String.join(", ", MOSCOW_ZONES));
        }
        return List.of();
    }

    public static LocalDate parseDdMmYyyy(String value) {
        try {
            return LocalDate.parse(value, DD_MM_YYYY_STRICT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static boolean isDdMmYyyyHhMmSs(String value) {
        return parsesAs(value, DD_MM_YYYY_HH_MM_SS_STRICT);
    }

    public static boolean parsesAs(String value, DateTimeFormatter formatter) {
        try {
            LocalDateTime.parse(value, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isDdMmYyyy(String value) {
        return parseDdMmYyyy(value) != null;
    }

}
