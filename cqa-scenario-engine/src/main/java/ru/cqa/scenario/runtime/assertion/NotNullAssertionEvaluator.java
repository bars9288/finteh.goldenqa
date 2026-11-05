package ru.cqa.scenario.runtime.assertion;

import ru.cqa.scenario.definition.model.Assertion;
import ru.cqa.scenario.runtime.execution.ExecutionContext;
import ru.cqa.scenario.runtime.expression.JsonPathReader;
import ru.cqa.scenario.runtime.result.MoveResult;

public final class NotNullAssertionEvaluator implements AssertionEvaluator {
    @Override
    public String getType() {
        return "notNull";
    }

    @Override
    public AssertionResult evaluate(Assertion assertion, MoveResult result, ExecutionContext context) {
        try {
            Object actual = JsonPathReader.read(result.response(), assertion.actualExpression());
            boolean expectedNotNull = Boolean.TRUE.equals(assertion.expectedValue());
            boolean passed = (actual != null) == expectedNotNull;
            return new AssertionResult(assertion.id(), passed ? AssertionStatus.PASSED : AssertionStatus.FAILED,
                    passed ? "Проверка null прошла" : "Фактическое значение не соответствует ожиданию notNull");
        } catch (RuntimeException exception) {
            return new AssertionResult(assertion.id(), AssertionStatus.ERROR, exception.getMessage());
        }
    }
}
