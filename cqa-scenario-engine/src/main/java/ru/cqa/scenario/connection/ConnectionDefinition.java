package ru.cqa.scenario.connection;

import java.util.Objects;

/** Ссылка на подключение из внешней конфигурации; секретные значения в модели не хранятся. */
public record ConnectionDefinition(String id, String type, String configurationKey) {

    public ConnectionDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(configurationKey, "configurationKey");
    }
}
