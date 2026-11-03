package ru.cqa.scenario.serialization;

import ru.cqa.scenario.model.Scenario;

/** Преобразует модель сценария в JSON для экспорта. */
public interface ScenarioSerializer {
    String serialize(Scenario scenario);
}
