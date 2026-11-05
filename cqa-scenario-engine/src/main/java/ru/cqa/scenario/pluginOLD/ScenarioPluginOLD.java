package ru.cqa.scenario.pluginOLD;

/** SPI-плагин, который может зарегистрировать исполнители и декларации действий. */
public interface ScenarioPluginOLD {
    PluginDescriptorOLD getDescriptor();

    void register(PluginContextOLD context, PluginRegistryOLD registry);
}
