package ru.cqa.scenario.plugin;

import java.util.Collection;
import ru.cqa.scenario.executor.MoveExecutor;

/** Упрощённый SPI для JAR, которые предоставляют только исполнители операций. */
public interface MoveExecutorProvider {
    PluginDescriptor getDescriptor();

    Collection<MoveExecutor<?>> getMoveExecutors(PluginContext context);
}
