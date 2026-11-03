package ru.cqa.scenario.plugin;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/** Нечувствительный контекст загрузки плагина; секреты подключений сюда не помещаются. */
public record PluginContext(Path jarPath, Map<String, String> options) {

    public PluginContext {
        Objects.requireNonNull(jarPath, "jarPath");
        options = Map.copyOf(Objects.requireNonNull(options, "options"));
    }
}
