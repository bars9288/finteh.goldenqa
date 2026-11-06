package ru.cqa.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

public final class DateValidationUtils {

    public static final List<String> MOSCOW_ZONES = List.of("МСК", "MSK", "+03:00", "Europe/Moscow");

    public static final DateTimeFormatter DD_MM_YYYY_STRICT = DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter DD_MM_YYYY_HH_MM_SS_STRICT = DateTimeFormatter.ofPattern("dd.MM.uuuu HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);

    private DateValidationUtils() {
    }

    public static List<String> validateDateTimeWithMoscowZone(String[] dateTimeParts, String fieldPath, String formatErrorMessage) {
        String errorPrefix = fieldPath + ": ";
        if ((dateTimeParts.length != 2 && dateTimeParts.length != 3) || !isValidDateTime(dateTimeParts[0] + " " + dateTimeParts[1])) {
            return List.of(errorPrefix + formatErrorMessage);
        }
        if (dateTimeParts.length == 3 && !MOSCOW_ZONES.contains(dateTimeParts[2])) {
            return List.of(errorPrefix + "часовой пояс должен быть московский: " + String.join(", ", MOSCOW_ZONES));
        }
        return List.of();
    }

    public static LocalDate parseDateOrNull(String value) {
        try {
            return LocalDate.parse(value, DD_MM_YYYY_STRICT);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    public static boolean isValidDateTime(String value) {
        return matchesDateTimeFormat(value, DD_MM_YYYY_HH_MM_SS_STRICT);
    }

    public static boolean matchesDateTimeFormat(String value, DateTimeFormatter formatter) {
        try {
            LocalDateTime.parse(value, formatter);
            return true;
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isValidDate(String value) {
        return parseDateOrNull(value) != null;
    }

}
