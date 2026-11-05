package ru.cqa.scenario.extract;

import ru.cqa.scenario.definition.model.Extract;
import ru.cqa.scenario.runtime.expression.JsonPathReader;
import ru.cqa.scenario.runtime.result.MoveResult;

public final class JsonPathExtractor implements Extractor {
    @Override
    public String getType() {
        return "jsonpath";
    }

    @Override
    public Object extract(Extract extract, MoveResult result) {
        return JsonPathReader.read(result.response(), extract.sourceExpression());
    }
}
