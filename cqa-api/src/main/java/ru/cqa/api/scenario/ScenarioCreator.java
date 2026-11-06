package ru.cqa.api.scenario;

import ru.cqa.model.scenario.Scenario;

/** Разбирает, проверяет и сохраняет новое описание сценария. */
public interface ScenarioCreator {
    Scenario create(String json);
}
