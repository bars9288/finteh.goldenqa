package ru.cqa.cases.mdm.constants;

import java.util.regex.Pattern;

public final class FieldPatterns {

    /* Значения по умолчанию для всех реквизитов */
    public static final String UNKNOWN = "UNKNOWN";

    /* Документы */
    public static final Pattern SNILS_FORMAT       = Pattern.compile("\\d{3}\\-\\d{3}\\-\\d{3}\\s\\d{2}");
    public static final Pattern CITIZENSHIP_FORMAT = Pattern.compile("\\d{1,3}");

    /* Даты */
    public static final Pattern DD_MM_YYYY         = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}");

    /* ФИО: первая буква заглавная, остальные строчные, только русские */
    public static final Pattern RU_CAPITALIZED     = Pattern.compile("[А-ЯЁ][а-яё]*");

    private FieldPatterns() {
    }
}
