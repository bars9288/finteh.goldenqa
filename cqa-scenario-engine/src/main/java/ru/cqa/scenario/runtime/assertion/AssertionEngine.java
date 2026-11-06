package ru.cqa.scenario.runtime.assertion;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ru.cqa.api.assertion.AssertionEvaluator;
import ru.cqa.api.execution.ExecutionContext;
import ru.cqa.model.assertion.AssertionResult;
import ru.cqa.model.execution.MoveResult;
import ru.cqa.model.scenario.Assertion;

/** Находит evaluator-ы по типу и агрегирует результаты набора утверждений. */
public final class AssertionEngine {
    private final Map<String, AssertionEvaluator> evaluators;

    public AssertionEngine(Collection<? extends AssertionEvaluator> evaluators) {
        Map<String, AssertionEvaluator> byType = new LinkedHashMap<>();
        for (AssertionEvaluator evaluator : evaluators) {
            Objects.requireNonNull(evaluator, "evaluator");
            if (byType.putIfAbsent(evaluator.getType(), evaluator) != null) {
                throw new IllegalArgumentException("AssertionEvaluator уже зарегистрирован: " + evaluator.getType());
            }
        }
        this.evaluators = Map.copyOf(byType);
    }

    public List<AssertionResult> evaluate(
            List<Assertion> assertions, MoveResult moveResult, ExecutionContext context) {
        Objects.requireNonNull(assertions, "assertions");
        Objects.requireNonNull(moveResult, "moveResult");
        Objects.requireNonNull(context, "context");
        return assertions.stream()
                .map(assertion -> {
                    AssertionEvaluator evaluator = evaluators.get(assertion.type());
                    if (evaluator == null) {
                        throw new IllegalArgumentException("Неизвестный тип Assertion: " + assertion.type());
                    }
                    return evaluator.evaluate(assertion, moveResult, context);
                })
                .toList();
    }
}
