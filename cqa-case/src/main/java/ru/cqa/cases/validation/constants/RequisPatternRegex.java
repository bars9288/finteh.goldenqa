package ru.cqa.cases.validation.constants;

import java.util.regex.Pattern;

public class RequisPatternRegex {
    /* Документы */
    public static final Pattern SNILS_FORMAT       = Pattern.compile("\\d{3}\\-\\d{3}\\-\\d{3}\\s\\d{2}");
    public static final Pattern CITIZENSHIP_FORMAT = Pattern.compile("\\d{1,3}");
    /* Даты */
    public static final Pattern DD_MM_YYYY         = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}");

    private RequisPatternRegex() {
    }
}
