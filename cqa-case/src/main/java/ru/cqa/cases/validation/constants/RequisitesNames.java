package ru.cqa.cases.validation.constants;

import java.util.Set;

public final class RequisitesNames {

    public static final String NAME                           = "name";
    public static final String SURNAME                        = "surname";
    public static final String PATRONYMIC                     = "patronymic";
    public static final String FULL_NAME_RAW_SOURCE           = "fullNameRawSource";
    public static final String BIRTHDATE                      = "birthdate";
    public static final String BIRTHDATE_RAW_SOURCE           = "birthdateRawSource";
    public static final String DEATH_DATE                     = "deathDate";
    public static final String GENDER                         = "gender";
    public static final String GENDER_RAW_SOURCE              = "genderRawSource";
    public static final String BIRTH_PLACE                    = "birthPlace";
    public static final String CITIZENSHIP                    = "citizenship";
    public static final String FOREIGN_MONETARY_RESIDENT_FLAG = "foreignMonetaryResidentFlag";
    public static final String INN                            = "inn";
    public static final String INN_RAW_SOURCE                 = "innRawSource";
    public static final String SNILS                          = "snils";
    public static final String SNILS_RAW_SOURCE               = "snilsRawSource";
    public static final String IDENTIFICATION_LEVEL           = "identificationLevel";
    public static final String VIP                            = "vip";
    public static final String BANK_EMPLOYEE_FLAG             = "bankEmployeeFlag";
    public static final String CREATED_DATE                   = "createdDate";
    public static final String ACTUALITY_DATE                 = "actualityDate";

    public static final Set<String> values = Set.of(NAME, SURNAME, PATRONYMIC, FULL_NAME_RAW_SOURCE, BIRTHDATE,
            BIRTHDATE_RAW_SOURCE, DEATH_DATE, GENDER, GENDER_RAW_SOURCE, BIRTH_PLACE, CITIZENSHIP, FOREIGN_MONETARY_RESIDENT_FLAG,
            INN, INN_RAW_SOURCE, SNILS, SNILS_RAW_SOURCE, IDENTIFICATION_LEVEL, VIP, BANK_EMPLOYEE_FLAG, CREATED_DATE, ACTUALITY_DATE);

    private RequisitesNames() {
    }
}
