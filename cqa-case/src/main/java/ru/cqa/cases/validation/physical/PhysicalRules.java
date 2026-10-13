package ru.cqa.cases.validation.physical;

import ru.cqa.dto.AttributeDTO;

import static ru.cqa.cases.validation.constants.AttrRequisNames.*;
import static ru.cqa.cases.validation.constants.AttrTypes.*;
import static ru.cqa.cases.validation.constants.RequisNames.*;
import static ru.cqa.cases.validation.constants.RequisGenderFields.*;
import static ru.cqa.cases.validation.constants.RequisBoolFields.*;
import static ru.cqa.cases.validation.utils.DateFormatUtils.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public final class PhysicalRules {

    private static final Set<String> GENDERS = Set.of(MALE, FEMALE);
    private static final Set<String> TRUE_FALSE_UNKNOWN = Set.of(TRUE, FALSE, UNKNOWN);
    private static final Set<String> PHYSICAL_ATTRIBUTE_TYPES = Set.of(DOCUMENT_PASSPORT, ADDRESS, PHONE, EMAIL, EXTERNAL_IDENTIFIER);

    public static final List<PhysicalRule> TO_SAVE_PHYSICAL = List.of(
//   *** ПРОВЕРКА ОБЯЗАТЕЛЬНЫХ КЛЮЧЕЙ ДЛЯ PARTY
//1. ОБЩАЯ проверка: Поле уровень идентификации обязателен
            party -> {
                if (isBlank(party.fieldMap().get(IDENTIFICATION_LEVEL))) {
                    return List.of("party[0].field." + IDENTIFICATION_LEVEL + ": обязательно, не должно быть пустым");
                }
                if (isBlank(party.fieldMap().get(IDENTIFICATION_LEVEL))) {
                    return List.of("party[0].field." + IDENTIFICATION_LEVEL + ": обязательно, не должно быть пустым");
                }
                return List.of();
            },
//2. ОБЩАЯ проверка: Дата создания — createdDate обязательна, формат ДД.ММ.ГГГГ или ДД.ММ.ГГГГ ЧЧ:мм:СС МСК; пояс необязателен, но если указан — только московский
            party -> {
                String raw = party.fieldMap().get(CREATED_DATE);
                if (isBlank(raw)) {
                    return List.of("party[0].field." + CREATED_DATE + ": обязательно, не должно быть пустым");
                }
                String[] parts = raw.strip().split("\\s");
                if (parts.length == 1 && isDdMmYyyy(parts[0])) {
                    return List.of();
                }
                return checkDateTimeZnMsk(parts, "party[0].field." + CREATED_DATE,
                        "ожидается дата в формате ДД.ММ.ГГГГ или ДД.ММ.ГГГГ ЧЧ:мм:СС, необязательный пояс");
            },

//3. ОБЩАЯ проверка: Дата актуальности — обязательна, формат ДД.ММ.ГГГГ ЧЧ:мм:СС МСК; пояс необязателен, но если указан — только московский
            party -> {
                String raw = party.fieldMap().get(ACTUALITY_DATE);
                if (isBlank(raw)) {
                    return List.of("party[0].field." + ACTUALITY_DATE + ": обязательно, не должно быть пустым");
                }
                return checkDateTimeZnMsk(raw.strip().split(" "), "party[0].field." + ACTUALITY_DATE, "ожидается дата и время в формате ДД.ММ.ГГГГ ЧЧ:мм:СС, после времени — необязательный пояс");
            },

//   *** ПРОВЕРКА ОБЯЗАТЕЛЬНЫХ КЛЮЧЕЙ ДЛЯ PARTY -> ATTRUBUTE -> FIELD
//4. Правило атрибута - ОБЩАЯ проверка: у каждого атрибута обязателен реквизит type (не null и не пустой)
            party -> {
                List<AttributeDTO> attrs = party.attribute();
                if (attrs == null) {
                    return List.of();
                }
                List<String> errors = new ArrayList<>();
                for (int i = 0; i < attrs.size(); i++) {
                    AttributeDTO a = attrs.get(i);
                    if (a != null && isBlank(a.fieldMap().get(ATTR_TYPE))) {
                        errors.add("party[0].attribute[" + i + "].field." + ATTR_TYPE + ": обязательно, не должно быть пустым");
                    }
                }
                return errors;
            },

//5. Правило атрибута - ОБЩАЯ проверка: ни у одного атрибута не допускается заполненный реквизит primary (null или пусто — ок)
            party -> {
                List<AttributeDTO> attrs = party.attribute();
                if (attrs == null) {
                    return List.of();
                }
                List<String> errors = new ArrayList<>();
                for (int i = 0; i < attrs.size(); i++) {
                    AttributeDTO a = attrs.get(i);
                    if (a != null && !isBlank(a.fieldMap().get(PRIMARY))) {
                        errors.add("party[0].attribute[" + i + "].field." + PRIMARY + ": не заполняется");
                    }
                }
                return errors;
            },

//6. Правило атрибута - ОБЩАЯ проверка: у каждого атрибута обязателен реквизит actualityDate,
//    формат ДД.ММ.ГГГГ ЧЧ:мм:СС МСК; пояс необязателен, но если указан — только московский
            party -> {
                List<AttributeDTO> attrs = party.attribute();
                if (attrs == null) {
                    return List.of();
                }
                List<String> errors = new ArrayList<>();
                for (int i = 0; i < attrs.size(); i++) {
                    AttributeDTO a = attrs.get(i);
                    if (a == null) {
                        continue;
                    }
                    String path = "party[0].attribute[" + i + "].field." + ACTUALITY_DATE;
                    String raw = a.fieldMap().get(ACTUALITY_DATE);
                    if (isBlank(raw)) {
                        errors.add(path + ": обязательно, не должно быть пустым");
                    } else {
                        errors.addAll(checkDateTimeZnMsk(raw.strip().split(" "), path, "ожидается дата и время в формате ДД.ММ.ГГГГ ЧЧ:мм:СС, после времени — необязательный пояс"));
                    }
                }
                return errors;
            },


// ЧАСТНЫЕ проверки РЕКВИЗИТОВ Физического лица
//7. Фамилия, Имя, Отчество передаётся только в name, surname, patronymic ИЛИ только в fullNameRawSource
            party -> {
                Map<String, String> f = party.fieldMap();
                List<String> fio = List.of(NAME, SURNAME, PATRONYMIC);

                boolean fioAll = fio.stream().noneMatch(n -> isBlank(f.get(n)));
                boolean fioAny = fio.stream().anyMatch(n -> !isBlank(f.get(n)));
                boolean full = !isBlank(f.get(FULL_NAME_RAW_SOURCE));

                if (full && fioAny) {
                    return List.of("party[0].field: заполнено и ФИО, и " + FULL_NAME_RAW_SOURCE + " — допустимо что-то одно");
                }
                if (!full && !fioAny) {
                    return List.of("party[0].field: ожидается либо " + String.join(", ", fio) + ", либо " + FULL_NAME_RAW_SOURCE);
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

//8. Дата рождения в формате ДД.ММ.ГГГГ передаётся только в birthdateRawSource
            party -> {
                Map<String, String> f = party.fieldMap();
                List<String> errors = new ArrayList<>();
                String birthdate = f.get(BIRTHDATE);
                if (!isBlank(birthdate) && DD_MM_YYYY.matcher(birthdate.strip()).matches()) {
                    errors.add("party[0].field." + BIRTHDATE + ": дату в формате ДД.ММ.ГГГГ указывайте в " + BIRTHDATE_RAW_SOURCE);
                }
                String raw = f.get(BIRTHDATE_RAW_SOURCE);
                if (!isBlank(raw) && !isDdMmYyyy(raw.strip())) {
                    errors.add("party[0].field." + BIRTHDATE_RAW_SOURCE + ": ожидается дата в формате ДД.ММ.ГГГГ");
                }
                return errors;
            },

//9. Дата смерти — только в deathDate, формат ДД.ММ.ГГГГ, позже даты рождения
            party -> {
                Map<String, String> f = party.fieldMap();
                String raw = f.get(DEATH_DATE);
                if (isBlank(raw)) {
                    return List.of();
                }
                LocalDate death = parseDdMmYyyy(raw.strip());
                if (death == null) {
                    return List.of("party[0].field." + DEATH_DATE + ": ожидается дата в формате ДД.ММ.ГГГГ");
                }
                String birthRaw = f.get(BIRTHDATE_RAW_SOURCE);
                LocalDate birth = isBlank(birthRaw) ? null : parseDdMmYyyy(birthRaw.strip());
                if (birth != null && !death.isAfter(birth)) {
                    return List.of("party[0].field." + DEATH_DATE + ": дата смерти должна быть позже даты рождения");
                }
                return List.of();
            },

//10. Пол — только в genderRawSource, значения MALE или FEMALE; поле gender не заполняется
            party -> {
                Map<String, String> f = party.fieldMap();
                List<String> errors = new ArrayList<>();
                if (!isBlank(f.get(GENDER))) {
                    errors.add("party[0].field." + GENDER + ": не заполняется, пол указывайте в " + GENDER_RAW_SOURCE);
                }
                String raw = f.get(GENDER_RAW_SOURCE);
                if (!isBlank(raw) && !GENDERS.contains(raw.toUpperCase().strip())) {
                    errors.add("party[0].field." + GENDER_RAW_SOURCE + ": допустимо MALE или FEMALE");
                }
                return errors;
            },

//11. Место рождения — только из birthPlace, обязательно; birthPlaceCountry, birthPlaceRegion и т.п. его не заменяют
            party -> {
                Map<String, String> f = party.fieldMap();
                if (isBlank(f.get(BIRTH_PLACE))) {
                    return List.of("party[0].field." + BIRTH_PLACE + ": не должно быть пустым — место рождения читается только из " + BIRTH_PLACE);
                }
                return List.of();
            },


//12. Код гражданства — от одной до трёх цифр (2, 12, 123) или UNKNOWN; пустое допустимо
            party -> {
                String raw = party.fieldMap().get(CITIZENSHIP);
                if (isBlank(raw)) {
                    return List.of();
                }
                String value = raw.strip();
                if (!CITIZENSHIP_DIGITS.matcher(value).matches() && !"UNKNOWN".equals(value)) {
                    return List.of("party[0].field." + CITIZENSHIP + ": ожидается от одной до трёх цифр или UNKNOWN");
                }
                return List.of();
            },

//13. Признак валютного резидентства — TRUE, FALSE или UNKNOWN, регистр не важен; пустое допустимо
            party -> {
                String raw = party.fieldMap().get(FOREIGN_MONETARY_RESIDENT_FLAG);
                if (!isBlank(raw) && !TRUE_FALSE_UNKNOWN.contains(raw.strip().toUpperCase(Locale.ROOT))) {
                    return List.of("party[0].field." + FOREIGN_MONETARY_RESIDENT_FLAG + ": допустимо TRUE, FALSE или UNKNOWN");
                }
                return List.of();
            },

//14. ИНН — только в innRawSource; поле inn не заполняется
            party -> {
                if (!isBlank(party.fieldMap().get(INN))) {
                    return List.of("party[0].field." + INN + ": не заполняется, ИНН указывайте в " + INN_RAW_SOURCE);
                }
                return List.of();
            },

//15. СНИЛС — только в snilsRawSource, формат 123-123-123 12; поле snils не заполняется
            party -> {
                Map<String, String> f = party.fieldMap();
                List<String> errors = new ArrayList<>();
                if (!isBlank(f.get(SNILS))) {
                    errors.add("party[0].field." + SNILS + ": не заполняется, СНИЛС указывайте в " + SNILS_RAW_SOURCE);
                }
                String raw = f.get(SNILS_RAW_SOURCE);
                if (!isBlank(raw) && !SNILS_FORMAT.matcher(raw.strip()).matches()) {
                    errors.add("party[0].field." + SNILS_RAW_SOURCE + ": ожидается формат 123-123-123 12");
                }
                return errors;
            },

//16. VIP — TRUE, FALSE или UNKNOWN; пустое допустимо
            party -> {
                String raw = party.fieldMap().get(VIP);
                if (!isBlank(raw) && !TRUE_FALSE_UNKNOWN.contains(raw.strip())) {
                    return List.of("party[0].field." + VIP + ": допустимо TRUE, FALSE или UNKNOWN");
                }
                return List.of();
            },

//17. Признак сотрудника банка — TRUE, FALSE или UNKNOWN; пустое допустимо
            party -> {
                String raw = party.fieldMap().get(BANK_EMPLOYEE_FLAG);
                if (!isBlank(raw) && !TRUE_FALSE_UNKNOWN.contains(raw.strip())) {
                    return List.of("party[0].field." + BANK_EMPLOYEE_FLAG + ": допустимо TRUE, FALSE или UNKNOWN");
                }
                return List.of();
            },

//   *** ЧАСТНАЯ проверка АТРИБУТОВ Физического Лица
//18. Правило сохранения атрибутов - ОБЩАЯ проверка на ВСЕ атрибуты
            party -> {
                List<AttributeDTO> attrs = party.attribute();
                if (attrs == null || attrs.isEmpty()) {
                    return List.of();
                }
                boolean typesNull = attrs.stream().anyMatch(a -> a == null || isBlank(a.type()));
                String typesErr = attrs.stream()
                        .filter(a -> a != null && !isBlank(a.type()) && !PHYSICAL_ATTRIBUTE_TYPES.contains(a.type()))
                        .map(AttributeDTO::type)
                        .collect(Collectors.joining(", "));
                if (typesErr.isEmpty() && !typesNull) {
                    return List.of();
                }
                if (typesErr.isEmpty()) {
                    return List.of("party[0].attribute[i]. Недопустимы пустые и null значения типа Атрибута ФЛ");
                }
                return List.of("party[0].attribute[i]. Недопустимые типы для Атрибута ФЛ: " + typesErr
                        + (typesNull ? ", а так же недопустимы пустые и null значения" : ""));
            },

//19. Правило атрибута - документ DOCUMENT_PASSPORT обязательно наличие реквизитов type=PASSPORT поля уточнения типа документа qualifierType а так же обязательных реквизитов;
//    реквизит rawSource, если передан, должен быть пустым
            party -> {
                List<AttributeDTO> attrs = party.attribute();
                if (attrs == null) {
                    return List.of();
                }
                List<String> errors = new ArrayList<>();
                for (int i = 0; i < attrs.size(); i++) {
                    AttributeDTO a = attrs.get(i);
                    if (a == null || !DOCUMENT_PASSPORT.equals(a.type())) {
                        continue;
                    }
                    Map<String, String> f = a.fieldMap();
                    String path = "party[0].attribute[" + i + "].field.";
                    String type = f.get(ATTR_TYPE);
                    if (isBlank(type) || !PASSPORT.equals(type.strip())) {
                        errors.add(path + ATTR_TYPE + ": для " + DOCUMENT_PASSPORT + " ожидается " + PASSPORT);
                    }
                    if (isBlank(f.get(QUALIFIER_TYPE))) {
                        errors.add(path + QUALIFIER_TYPE + ": обязательно для " + DOCUMENT_PASSPORT);
                    }
                    if (!isBlank(f.get(RAW_SOURCE))) {
                        errors.add(path + RAW_SOURCE + ": должно быть пустым");
                    }
                }
                return errors;
            },

//20. Правило атрибута - адрес ADDRESS: реквизит type обязателен (не null и не пустой)
            party -> {
                List<AttributeDTO> attrs = party.attribute();
                if (attrs == null) {
                    return List.of();
                }
                List<String> errors = new ArrayList<>();
                for (int i = 0; i < attrs.size(); i++) {
                    AttributeDTO a = attrs.get(i);
                    if (a == null || !ADDRESS.equals(a.type())) {
                        continue;
                    }
                    if (isBlank(a.fieldMap().get(ATTR_TYPE))) {
                        errors.add("party[0].attribute[" + i + "].field." + ATTR_TYPE + ": обязательно для " + ADDRESS);
                    }
                }
                return errors;
            },

//21. Правило атрибута - телефон PHONE: телефон передаётся строкой в реквизите rawSource, обязателен (не null и не пустой)
            party -> {
                List<AttributeDTO> attrs = party.attribute();
                if (attrs == null) {
                    return List.of();
                }
                List<String> errors = new ArrayList<>();
                for (int i = 0; i < attrs.size(); i++) {
                    AttributeDTO a = attrs.get(i);
                    if (a == null || !PHONE.equals(a.type())) {
                        continue;
                    }
                    if (isBlank(a.fieldMap().get(RAW_SOURCE))) {
                        errors.add("party[0].attribute[" + i + "].field." + RAW_SOURCE + ": обязательно для " + PHONE);
                    }
                }
                return errors;
            },

//22. Правило атрибута - почта EMAIL: адрес передаётся строкой в реквизите rawSource, обязателен и содержит @
            party -> {
                List<AttributeDTO> attrs = party.attribute();
                if (attrs == null) {
                    return List.of();
                }
                List<String> errors = new ArrayList<>();
                for (int i = 0; i < attrs.size(); i++) {
                    AttributeDTO a = attrs.get(i);
                    if (a == null || !EMAIL.equals(a.type())) {
                        continue;
                    }
                    String path = "party[0].attribute[" + i + "].field." + RAW_SOURCE;
                    String raw = a.fieldMap().get(RAW_SOURCE);
                    if (isBlank(raw)) {
                        errors.add(path + ": обязательно для " + EMAIL);
                    } else if (!raw.contains("@")) {
                        errors.add(path + ": для " + EMAIL + " ожидается адрес со знаком @");
                    }
                }
                return errors;
            }
    );


    public static final List<PhysicalRule> TO_READ_PHYSICAL = List.of(
            party -> {return List.of();}
    );

    private PhysicalRules() {
    }
}
