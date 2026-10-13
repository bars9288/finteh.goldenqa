package ru.cqa.cases.validation.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.regex.Pattern;

public final class DateFormatUtils {

    public static final Pattern SNILS_FORMAT = Pattern.compile("\\d{3}\\-\\d{3}\\-\\d{3}\\s\\d{2}");
    public static final Pattern DD_MM_YYYY = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}");
    public static final Pattern CITIZENSHIP_DIGITS = Pattern.compile("\\d{1,3}");

    public static final List<String> MOSCOW_ZONES = List.of("МСК", "MSK", "+03:00", "Europe/Moscow");

    public static final DateTimeFormatter DD_MM_YYYY_STRICT = DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter DD_MM_YYYY_HH_MM_SS_STRICT = DateTimeFormatter.ofPattern("dd.MM.uuuu HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);

    private DateFormatUtils() {
    }

    /**
     * Дата со временем и необязательным поясом: {@code ДД.ММ.ГГГГ ЧЧ:мм:СС [пояс]}, части через один пробел.
     * Пояс, если указан, — только московский ({@link #MOSCOW_ZONES}).
     */
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

    /** Дата из строки ДД.ММ.ГГГГ; {@code null}, если формат не тот или такой даты нет. */
    public static LocalDate parseDdMmYyyy(String value) {
        try {
            return LocalDate.parse(value, DD_MM_YYYY_STRICT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static boolean parsesAs(String value, DateTimeFormatter formatter) {
        try {
            LocalDateTime.parse(value, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isDdMmYyyyHhMmSs(String value) {
        return parsesAs(value, DD_MM_YYYY_HH_MM_SS_STRICT);
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isDdMmYyyy(String value) {
        return parseDdMmYyyy(value) != null;
    }

}
