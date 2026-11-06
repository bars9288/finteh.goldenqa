package ru.cqa.scenario.legacy.plugin;

import java.util.Objects;

/** Публичная идентификация расширения без его внутренней реализации. */
public record LegacyPluginDescriptor(String id, String version, String description) {

    public LegacyPluginDescriptor {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(version, "version");
        description = description == null ? "" : description;
    }
}
