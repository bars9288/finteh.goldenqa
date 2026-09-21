package ru.cqa.business.greeting;

/**
 * Внешний сервис, участвующий в сценарии (блок {@code servicesInfo}).
 *
 * <p>Второй тип адресата — нужен, чтобы шаблон был не теоретическим:
 * на нём видно, как реестр выбирает реализацию по типу.
 *
 * @param typeService имя сервиса, например {@code KAFKA-CLIENT}
 * @param scope       контур, например {@code DEV}
 */
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
