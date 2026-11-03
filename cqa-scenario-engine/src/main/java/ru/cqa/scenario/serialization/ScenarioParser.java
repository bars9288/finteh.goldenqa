package ru.cqa.scenario.serialization;

import ru.cqa.scenario.model.Scenario;

/** Преобразует JSON-представление сценария в модель ядра. */
public interface ScenarioParser {
    Scenario parse(String json);
}
