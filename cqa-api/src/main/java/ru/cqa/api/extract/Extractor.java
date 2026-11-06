package ru.cqa.api.extract;

import ru.cqa.model.execution.MoveResult;
import ru.cqa.model.scenario.Extract;

/** Извлекает значение определённого формата из результата операции. */
public interface Extractor {
    String getType();

    Object extract(Extract extract, MoveResult result);
}
