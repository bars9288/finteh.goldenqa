package ru.cqa.scenario.legacy.plugin;

/** SPI-плагин, который может зарегистрировать исполнители и декларации действий. */
public interface LegacyScenarioPlugin {
    LegacyPluginDescriptor getDescriptor();

    void register(LegacyPluginContext context, LegacyPluginRegistry registry);
}
