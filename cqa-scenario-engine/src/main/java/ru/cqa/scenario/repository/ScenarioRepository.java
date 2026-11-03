package ru.cqa.scenario.repository;

import java.util.Optional;
import java.util.UUID;
import ru.cqa.scenario.model.Scenario;

/** Порт хранения; PostgreSQL-реализация будет вынесена в инфраструктурный модуль. */
public interface ScenarioRepository {
    Optional<Scenario> findById(UUID id);

    Optional<Scenario> findByIdAndVersion(UUID id, String version);

    Scenario save(Scenario scenario);

    void delete(UUID id);
}
