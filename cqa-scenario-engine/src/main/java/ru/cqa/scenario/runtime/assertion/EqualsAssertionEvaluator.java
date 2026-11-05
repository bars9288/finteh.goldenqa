package ru.cqa.scenario.runtime.assertion;

import java.util.Objects;

import ru.cqa.scenario.definition.model.Assertion;
import ru.cqa.scenario.runtime.execution.ExecutionContext;
import ru.cqa.scenario.runtime.expression.JsonPathReader;
import ru.cqa.scenario.runtime.result.MoveResult;

public final class EqualsAssertionEvaluator implements AssertionEvaluator {
    @Override
    public String getType() {
        return "equals";
    }

    @Override
    public AssertionResult evaluate(Assertion assertion, MoveResult result, ExecutionContext context) {
        try {
            Object actual = JsonPathReader.read(result.response(), assertion.actualExpression());
            boolean passed = Objects.equals(actual, assertion.expectedValue());
            String message = passed
                    ? "Фактическое значение совпало с ожидаемым"
                    : "Ожидалось '" + assertion.expectedValue() + "', получено '" + actual + "'";
            return new AssertionResult(assertion.id(), passed ? AssertionStatus.PASSED : AssertionStatus.FAILED, message);
        } catch (RuntimeException exception) {
            return new AssertionResult(assertion.id(), AssertionStatus.ERROR, exception.getMessage());
        }
    }
}
