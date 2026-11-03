package ru.cqa.scenario.execution;

import ru.cqa.scenario.model.Move;
import ru.cqa.scenario.result.MoveResult;

/** Передаёт операцию подходящему зарегистрированному исполнителю. */
public interface MoveRunner {
    MoveResult execute(Move move, ExecutionContext context);
}
