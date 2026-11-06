package ru.cqa.cases.mdm.constants;

import java.util.Set;

public final class BooleanFieldValues {

    public static final String TRUE        = "TRUE";
    public static final String FALSE       = "FALSE";
    public static final String UNKNOWN     = "UNKNOWN";

    public static final Set<String> ALLOWED_VALUES = Set.of(TRUE, FALSE, UNKNOWN);

    private BooleanFieldValues() {
    }
}
