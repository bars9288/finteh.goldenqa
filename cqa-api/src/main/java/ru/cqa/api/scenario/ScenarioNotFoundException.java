package ru.cqa.api.scenario;

import java.util.UUID;

public final class ScenarioNotFoundException extends RuntimeException {
    public ScenarioNotFoundException(UUID scenarioId, String version) {
        super("Сценарий " + scenarioId + " версии " + version + " не найден");
    }
}
