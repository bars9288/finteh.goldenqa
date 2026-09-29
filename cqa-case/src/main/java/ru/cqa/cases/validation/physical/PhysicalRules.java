package ru.cqa.cases.validation.physical;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Каталог правил : каждое правило — лямбда {@link PhysicalRule},
 * выполняются по порядку списка.
 */
public final class PhysicalRules {

    private static final Pattern DD_MM_YYYY = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}");
    private static final DateTimeFormatter DD_MM_YYYY_STRICT = DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final Set<String> GENDERS = Set.of("MALE", "FEMALE");

    public static final List<PhysicalRule> TO_SAVE_PHYSICAL = List.of(

            //1. Фамилия, Имя, Отчество передаётся только в name, surname, patronymic ИЛИ только в fullNameRawSource
            party -> {
                Map<String, String> f = party.fieldMap();
                List<String> fio = List.of("name", "surname", "patronymic");

                boolean fioAll = fio.stream().noneMatch(n -> isBlank(f.get(n)));
                boolean fioAny = fio.stream().anyMatch(n -> !isBlank(f.get(n)));
                boolean full   = !isBlank(f.get("fullNameRawSource"));

                if (full && fioAny) {
                    return List.of("party[0].field: заполнено и ФИО, и fullNameRawSource — допустимо что-то одно");
                }
                if (!full && !fioAny) {
                    return List.of("party[0].field: ожидается либо name, surname, patronymic, либо fullNameRawSource");
                }
                if (!full && !fioAll) {
                    List<String> errors = new ArrayList<>();
                    for (String n : fio) {
                        if (isBlank(f.get(n))) {
                            errors.add("party[0].field." + n + ": не должно быть пустым");
                        }
                    }
                    return errors;
                }
                return List.of();
            },

            //2. Дата рождения в формате ДД.ММ.ГГГГ передаётся только в birthdateRawSource
            party -> {
                Map<String, String> f = party.fieldMap();
                List<String> errors = new ArrayList<>();
                String birthdate = f.get("birthdate");
                if (!isBlank(birthdate) && DD_MM_YYYY.matcher(birthdate.strip()).matches()) {
                    errors.add("party[0].field.birthdate: дату в формате ДД.ММ.ГГГГ указывайте в birthdateRawSource");
                }
                String raw = f.get("birthdateRawSource");
                if (!isBlank(raw) && !isDdMmYyyy(raw.strip())) {
                    errors.add("party[0].field.birthdateRawSource: ожидается дата в формате ДД.ММ.ГГГГ");
                }
                return errors;
            },

            //3. Дата смерти — только в deathDate, формат ДД.ММ.ГГГГ, позже даты рождения
            party -> {
                Map<String, String> f = party.fieldMap();
                String raw = f.get("deathDate");
                if (isBlank(raw)) {
                    return List.of();
                }
                LocalDate death = parseDdMmYyyy(raw.strip());
                if (death == null) {
                    return List.of("party[0].field.deathDate: ожидается дата в формате ДД.ММ.ГГГГ");
                }
                String birthRaw = f.get("birthdateRawSource");
                LocalDate birth = isBlank(birthRaw) ? null : parseDdMmYyyy(birthRaw.strip());
                if (birth != null && !death.isAfter(birth)) {
                    return List.of("party[0].field.deathDate: дата смерти должна быть позже даты рождения");
                }
                return List.of();
            },

            //4. Пол — только в genderRawSource, значения MALE или FEMALE; поле gender не заполняется
            party -> {
                Map<String, String> f = party.fieldMap();
                List<String> errors = new ArrayList<>();
                if (!isBlank(f.get("gender"))) {
                    errors.add("party[0].field.gender: не заполняется, пол указывайте в genderRawSource");
                }
                String raw = f.get("genderRawSource");
                if (!isBlank(raw) && !GENDERS.contains(raw.strip())) {
                    errors.add("party[0].field.genderRawSource: допустимо MALE или FEMALE");
                }
                return errors;
            }

    );


    public static final List<PhysicalRule> TO_READ_PHYSICAL = List.of(
            party -> {return List.of();}
    );

private PhysicalRules() {
}

public static boolean isNullEmpty(String value) {
    return value == null || value.isEmpty();
}

    /** {@code null}, пустая строка или одни пробелы — как у {@code @NotBlank}. */
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean isDdMmYyyy(String value) {
        return parseDdMmYyyy(value) != null;
    }

    /** Дата из строки ДД.ММ.ГГГГ; {@code null}, если формат не тот или такой даты нет. */
    private static LocalDate parseDdMmYyyy(String value) {
        try {
            return LocalDate.parse(value, DD_MM_YYYY_STRICT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
