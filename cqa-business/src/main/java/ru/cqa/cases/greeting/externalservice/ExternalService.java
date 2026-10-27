package ru.cqa.cases.greeting.externalservice;

public record ExternalService(String typeService, String scope) {

    public ExternalService {
        typeService = requireFilled(typeService, "typeService");
        scope = requireFilled(scope, "scope");
    }

    private static String requireFilled(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Поле '" + field + "' обязательно и не может быть пустым");
        }
        return value.strip();
    }
}
