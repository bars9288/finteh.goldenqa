package ru.cqa.scenario.plugin;

/** SPI-плагин, который может зарегистрировать исполнители и декларации действий. */
public interface ScenarioPlugin {
    PluginDescriptor getDescriptor();

    void register(PluginContext context, PluginRegistry registry);
}
