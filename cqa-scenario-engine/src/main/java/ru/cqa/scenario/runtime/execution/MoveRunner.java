package ru.cqa.scenario.runtime.execution;

import ru.cqa.api.execution.ExecutionContext;
import ru.cqa.model.execution.MoveResult;
import ru.cqa.model.scenario.Move;

/** Передаёт операцию подходящему зарегистрированному исполнителю. */
public interface MoveRunner {
    MoveResult execute(Move move, ExecutionContext context);
}
