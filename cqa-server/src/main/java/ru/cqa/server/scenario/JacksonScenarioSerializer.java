package ru.cqa.server.scenario;

import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import ru.cqa.scenario.definition.model.Scenario;
import ru.cqa.scenario.serialization.ScenarioSerializer;

/** Jackson-адаптер сериализации сценария для файлового хранилища. */
@Component
public final class JacksonScenarioSerializer implements ScenarioSerializer {
    private final JsonMapper mapper;

    public JacksonScenarioSerializer(JsonMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String serialize(Scenario scenario) {
        try {
            return mapper.writeValueAsString(scenario);
        } catch (JacksonException exception) {
            throw new ScenarioStorageException("Не удалось сериализовать сценарий " + scenario.id(), exception);
        }
    }
}
