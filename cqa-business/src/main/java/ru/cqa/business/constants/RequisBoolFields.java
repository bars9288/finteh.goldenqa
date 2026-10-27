package ru.cqa.business.constants;

import java.util.Set;

public final class RequisBoolFields {

    public static final String TRUE        = "TRUE";
    public static final String FALSE       = "FALSE";
    public static final String UNKNOWN     = "UNKNOWN";

    public static final Set<String> BoolValues = Set.of(TRUE, FALSE, UNKNOWN);

    private RequisBoolFields() {
    }
}
