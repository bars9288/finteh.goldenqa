package ru.cqa.scenario.assertion;

import ru.cqa.scenario.execution.ExecutionContext;
import ru.cqa.scenario.model.Assertion;
import ru.cqa.scenario.result.MoveResult;

/** Реализация проверки одного типа Assertion. */
public interface AssertionEvaluator {
    String getType();

    AssertionResult evaluate(Assertion assertion, MoveResult result, ExecutionContext context);
}
