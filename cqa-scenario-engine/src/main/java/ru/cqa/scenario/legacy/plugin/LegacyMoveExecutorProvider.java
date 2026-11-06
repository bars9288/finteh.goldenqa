package ru.cqa.scenario.legacy.plugin;

import java.util.Collection;
import ru.cqa.api.execution.MoveExecutor;

/** Упрощённый SPI для JAR, которые предоставляют только исполнители операций. */
public interface LegacyMoveExecutorProvider {
    LegacyPluginDescriptor getDescriptor();

    Collection<MoveExecutor<?>> getMoveExecutors(LegacyPluginContext context);
}
