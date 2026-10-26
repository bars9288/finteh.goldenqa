package ru.cqa.cases.validation.constants;

import java.util.Set;

public final class AttributesTypes {

    /* Общие */
    public static final String ADDRESS             = "ADDRESS";
    public static final String PHONE               = "PHONE";
    public static final String EMAIL               = "EMAIL";
    public static final String EXTERNAL_IDENTIFIER = "EXTERNAL_IDENTIFIER";
    /* ФЛ */
    public static final String DOCUMENT_PASSPORT   = "DOCUMENT_PASSPORT";
    /* ЮЛ */
    public static final String FLAG                = "FLAG";
    public static final String TIN                 = "TIN";
    public static final String CERTIFICATE         = "CERTIFICATE";
    public static final String DSMP                = "DSMP";
    public static final String TAX_RESIDENCE       = "TAX_RESIDENCE";
    /* КОСТЫЛЬ - Решение от Вендора для документов хардкодить type=PASSPORT а для уточнения есть qualifierType */
    public static final String PASSPORT            = "PASSPORT";

    public static final Set<String> physicalValues = Set.of(ADDRESS, PHONE, EMAIL, EXTERNAL_IDENTIFIER,
            DOCUMENT_PASSPORT);

    public static final Set<String> legalValues    = Set.of(ADDRESS, PHONE, EMAIL, EXTERNAL_IDENTIFIER,
            FLAG, TIN, CERTIFICATE, DSMP, TAX_RESIDENCE);

    private AttributesTypes() {
    }
}
