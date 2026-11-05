package ru.cqa.scenario.runtime.execution;

import ru.cqa.scenario.definition.model.Move;
import ru.cqa.scenario.runtime.result.MoveResult;

/** Передаёт операцию подходящему зарегистрированному исполнителю. */
public interface MoveRunner {
    MoveResult execute(Move move, ExecutionContext context);
}
