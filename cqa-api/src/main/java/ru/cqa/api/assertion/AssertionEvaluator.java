package ru.cqa.api.assertion;

import ru.cqa.api.execution.ExecutionContext;
import ru.cqa.model.assertion.AssertionResult;
import ru.cqa.model.execution.MoveResult;
import ru.cqa.model.scenario.Assertion;

/** Реализация проверки одного типа Assertion. */
public interface AssertionEvaluator {

    String getType();

    AssertionResult evaluate(Assertion assertion, MoveResult result, ExecutionContext context);
}
