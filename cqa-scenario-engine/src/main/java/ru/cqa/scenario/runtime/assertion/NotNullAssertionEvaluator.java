package ru.cqa.scenario.runtime.assertion;

import ru.cqa.api.assertion.AssertionEvaluator;
import ru.cqa.api.execution.ExecutionContext;
import ru.cqa.model.assertion.AssertionResult;
import ru.cqa.model.assertion.AssertionStatus;
import ru.cqa.model.execution.MoveResult;
import ru.cqa.model.scenario.Assertion;
import ru.cqa.scenario.runtime.expression.JsonPathReader;

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
