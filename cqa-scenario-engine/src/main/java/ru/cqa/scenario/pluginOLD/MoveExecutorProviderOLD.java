package ru.cqa.scenario.pluginOLD;

import java.util.Collection;
import ru.cqa.scenario.runtime.executor.MoveExecutor;

/** Упрощённый SPI для JAR, которые предоставляют только исполнители операций. */
public interface MoveExecutorProviderOLD {
    PluginDescriptorOLD getDescriptor();

    Collection<MoveExecutor<?>> getMoveExecutors(PluginContextOLD context);
}
