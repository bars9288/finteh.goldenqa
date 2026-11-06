package ru.cqa.api.scenario;

import ru.cqa.model.scenario.Scenario;

/** Преобразует модель сценария в JSON для экспорта. */
public interface ScenarioSerializer {
    String serialize(Scenario scenario);
}
