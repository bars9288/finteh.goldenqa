package ru.cqa.scenario.pluginOLD;

import java.util.Objects;

/** Публичная идентификация расширения без его внутренней реализации. */
public record PluginDescriptorOLD(String id, String version, String description) {

    public PluginDescriptorOLD {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(version, "version");
        description = description == null ? "" : description;
    }
}
