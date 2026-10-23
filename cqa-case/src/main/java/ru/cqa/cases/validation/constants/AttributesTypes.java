package ru.cqa.cases.validation.constants;

import java.util.Set;

public final class AttributesTypes {

    public static final String DOCUMENT_PASSPORT   = "DOCUMENT_PASSPORT";;
    public static final String ADDRESS             = "ADDRESS";
    public static final String PHONE               = "PHONE";
    public static final String EMAIL               = "EMAIL";
    public static final String EXTERNAL_IDENTIFIER = "EXTERNAL_IDENTIFIER";

    /* Решение от Вендора для документов type=PASSPORT а для уточнения есть qualifierType */
    public static final String PASSPORT = "PASSPORT";

    public static final Set<String> physicalValues = Set.of(DOCUMENT_PASSPORT, ADDRESS, PHONE, EMAIL, EXTERNAL_IDENTIFIER);

    private AttributesTypes() {
    }
}
