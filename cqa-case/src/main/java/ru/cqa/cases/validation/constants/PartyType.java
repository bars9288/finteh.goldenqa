package ru.cqa.cases.validation.constants;

import java.util.Arrays;
import java.util.Optional;

public enum PartyType {
    PHYSICAL,
    LEGAL;

    public static Optional<PartyType> of(String value) {
        return Arrays.stream(values()).filter(t -> t.name().equals(value)).findFirst();
    }
}
