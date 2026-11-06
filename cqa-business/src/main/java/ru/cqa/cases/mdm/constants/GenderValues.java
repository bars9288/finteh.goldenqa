package ru.cqa.cases.mdm.constants;

import java.util.Set;

public final class GenderValues {

    public static final String MALE       = "MALE";
    public static final String FEMALE     = "FEMALE";

    public static final  Set<String> ALLOWED_VALUES = Set.of(MALE, FEMALE);

    private GenderValues() {
    }
}
