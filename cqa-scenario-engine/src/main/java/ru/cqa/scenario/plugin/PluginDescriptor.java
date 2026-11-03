package ru.cqa.scenario.plugin;

import java.util.Objects;

/** Публичная идентификация расширения без его внутренней реализации. */
public record PluginDescriptor(String id, String version, String description) {

    public PluginDescriptor {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(version, "version");
        description = description == null ? "" : description;
    }
}
