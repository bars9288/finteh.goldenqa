package ru.cqa.scenario.runtime.assertion;

import ru.cqa.scenario.runtime.execution.ExecutionContext;
import ru.cqa.scenario.definition.model.Assertion;
import ru.cqa.scenario.runtime.result.MoveResult;

/** Реализация проверки одного типа Assertion. */
public interface AssertionEvaluator {

    String getType();

    AssertionResult evaluate(Assertion assertion, MoveResult result, ExecutionContext context);
}
