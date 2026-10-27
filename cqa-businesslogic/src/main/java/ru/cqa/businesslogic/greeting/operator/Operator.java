package ru.cqa.businesslogic.greeting.operator;

public record Operator(
        String name,
        String family
) {

    public Operator {
        name = requireFilled(name, "name");
        family = requireFilled(family, "family");
    }

    private static String requireFilled(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Поле '" + field + "' обязательно и не может быть пустым");
        }
        return value.strip();
    }

    public String shortName() {
        return name + " " + family;
    }
}
