package ru.cqa.api.scenario;

import java.util.Optional;
import java.util.UUID;
import ru.cqa.model.scenario.Scenario;

/** Порт хранения; PostgreSQL-реализация будет вынесена в инфраструктурный модуль. */
public interface ScenarioRepository {
    Optional<Scenario> findById(UUID scenarioId);

    Optional<Scenario> findByIdAndVersion(UUID scenarioId, String version);

    Scenario save(Scenario scenario);

    void delete(UUID scenarioId);
}
