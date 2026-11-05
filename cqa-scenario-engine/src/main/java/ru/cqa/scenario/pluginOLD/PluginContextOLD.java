package ru.cqa.scenario.pluginOLD;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/** Нечувствительный контекст загрузки плагина; секреты подключений сюда не помещаются. */
public record PluginContextOLD(Path jarPath, Map<String, String> options) {

    public PluginContextOLD {
        Objects.requireNonNull(jarPath, "jarPath");
        options = Map.copyOf(Objects.requireNonNull(options, "options"));
    }
}
