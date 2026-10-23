package ru.cqa.cases.validation.constants;


import java.util.Set;

public final class AttributesRequisitesNames {

    public static final String ATTR_TYPE      = "type";
    public static final String QUALIFIER_TYPE = "qualifierType";
    public static final String RAW_SOURCE     = "rawSource";
    public static final String PRIMARY        = "primary";

    public static final Set<String> values = Set.of(ATTR_TYPE, QUALIFIER_TYPE, RAW_SOURCE, PRIMARY);

    private AttributesRequisitesNames() {
    }
}
