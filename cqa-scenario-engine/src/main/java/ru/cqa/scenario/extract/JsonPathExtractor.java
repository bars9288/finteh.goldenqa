package ru.cqa.scenario.extract;

import ru.cqa.api.extract.Extractor;
import ru.cqa.model.execution.MoveResult;
import ru.cqa.model.scenario.Extract;
import ru.cqa.scenario.runtime.expression.JsonPathReader;

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
