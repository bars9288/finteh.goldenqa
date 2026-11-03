package ru.cqa.scenario.extract;

import ru.cqa.scenario.model.Extract;
import ru.cqa.scenario.result.MoveResult;

/** Извлекает значение определённого формата из результата операции. */
public interface Extractor {
    String getType();

    Object extract(Extract extract, MoveResult result);
}
