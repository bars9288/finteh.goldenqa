package ru.cqa.scenario.validation;

import java.util.List;
import ru.cqa.scenario.model.Scenario;

/** Контракт статической проверки модели сценария до прогона или сохранения. */
public interface ScenarioValidator {
    List<ValidationIssue> validate(Scenario scenario);
}
