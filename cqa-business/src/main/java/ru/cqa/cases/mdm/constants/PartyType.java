package ru.cqa.cases.mdm.constants;

import java.util.Arrays;
import java.util.Optional;

public enum PartyType {
    PHYSICAL,
    LEGAL;

    public static Optional<PartyType> of(String value) {
        return Arrays.stream(values()).filter(partyType -> partyType.name().equals(value)).findFirst();
    }
}
