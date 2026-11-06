package ru.cqa.server.scenario;

import java.time.DateTimeException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import ru.cqa.api.scenario.ScenarioParser;
import ru.cqa.model.scenario.Dependency;
import ru.cqa.model.scenario.Execution;
import ru.cqa.model.scenario.Move;
import ru.cqa.model.scenario.Scenario;
import ru.cqa.model.scenario.ScenarioMetadata;
import ru.cqa.model.scenario.Step;
import ru.cqa.model.scenario.move.HttpMove;
import ru.cqa.model.scenario.move.KafkaMove;
import ru.cqa.model.scenario.move.SoapMove;
import ru.cqa.model.scenario.move.SqlMove;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/** Jackson-адаптер: поле type выбирает конкретную модель Move. */
@Component
public final class JacksonScenarioParser implements ScenarioParser {
    private final JsonMapper jsonMapper;

    public JacksonScenarioParser(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Scenario parse(String json) {
        try {
            JsonNode scenarioNode = jsonMapper.readTree(json);
            if (scenarioNode == null || !scenarioNode.isObject()) {
                throw new IllegalArgumentException("Корень сценария должен быть JSON-объектом");
            }

            JsonNode metadataNode = scenarioNode.get("metadata");
            ScenarioMetadata metadata = metadataNode == null || metadataNode.isNull()
                    ? null
                    : jsonMapper.treeToValue(metadataNode, ScenarioMetadata.class);
            List<Dependency> dependencies = readObjects(scenarioNode.get("dependencies"), Dependency.class);
            Execution execution = readExecution(scenarioNode.get("execution"));
            List<Step> steps = readSteps(scenarioNode.get("steps"));

            JsonNode idNode = scenarioNode.get("id");
            UUID scenarioId = idNode == null || idNode.isNull() ? null : UUID.fromString(stringValue(idNode, "id"));
            return new Scenario(scenarioId, optionalString(scenarioNode, "version"), metadata, dependencies, execution, steps);
        } catch (JacksonException | DateTimeException exception) {
            throw new IllegalArgumentException("JSON сценария имеет неверный формат: " + exception.getMessage(), exception);
        }
    }

    private Execution readExecution(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        JsonNode timeoutNode = node.get("timeout");
        Duration timeout = timeoutNode == null || timeoutNode.isNull()
                ? null
                : Duration.parse(stringValue(timeoutNode, "execution.timeout"));
        return new Execution(timeout, node.path("continueAfterFailure").asBoolean(false));
    }

    private List<Step> readSteps(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        requireArray(node, "steps");
        List<Step> steps = new ArrayList<>();
        for (JsonNode stepNode : node) {
            steps.add(new Step(
                    optionalString(stepNode, "id"),
                    optionalString(stepNode, "name"),
                    optionalString(stepNode, "description"),
                    stepNode.path("continueOnError").asBoolean(false),
                    readMoves(stepNode.get("moves"))));
        }
        return steps;
    }

    private List<Move> readMoves(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        requireArray(node, "moves");
        List<Move> moves = new ArrayList<>();
        for (JsonNode moveNode : node) {
            if (!moveNode.isObject()) {
                throw new IllegalArgumentException("Элемент moves должен быть JSON-объектом");
            }
            String type = requiredText(moveNode, "type");
            ObjectNode properties = ((ObjectNode) moveNode).deepCopy();
            properties.remove("type");
            moves.add(switch (type) {
                case "http" -> jsonMapper.treeToValue(properties, HttpMove.class);
                case "sql" -> jsonMapper.treeToValue(properties, SqlMove.class);
                case "kafka" -> jsonMapper.treeToValue(properties, KafkaMove.class);
                case "soap" -> jsonMapper.treeToValue(properties, SoapMove.class);
                default -> throw new IllegalArgumentException("Неизвестный тип Move: " + type);
            });
        }
        return moves;
    }

    private <T> List<T> readObjects(JsonNode node, Class<T> type) {
        if (node == null || node.isNull()) {
            return null;
        }
        requireArray(node, "dependencies");
        List<T> values = new ArrayList<>();
        for (JsonNode item : node) {
            values.add(jsonMapper.treeToValue(item, type));
        }
        return values;
    }

    private static String requiredText(JsonNode node, String field) {
        String value = optionalString(node, field);
        if (value == null) {
            throw new IllegalArgumentException("Обязательное поле отсутствует: " + field);
        }
        return value;
    }

    private static String optionalString(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : stringValue(value, field);
    }

    private static String stringValue(JsonNode node, String field) {
        if (!node.isTextual()) {
            throw new IllegalArgumentException("Поле " + field + " должно быть строкой");
        }
        return node.asText();
    }

    private static void requireArray(JsonNode node, String field) {
        if (!node.isArray()) {
            throw new IllegalArgumentException("Поле " + field + " должно быть массивом");
        }
    }
}
