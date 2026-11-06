package ru.cqa.api.scenario;

import ru.cqa.model.scenario.Scenario;

/** Преобразует JSON-представление сценария в модель ядра. */
public interface ScenarioParser {
    Scenario parse(String json);
}
