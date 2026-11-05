package ru.cqa.server.scenario;

import java.time.DateTimeException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.json.JsonMapper;
import ru.cqa.scenario.definition.model.Dependency;
import ru.cqa.scenario.definition.model.Execution;
import ru.cqa.scenario.definition.model.Move;
import ru.cqa.scenario.definition.model.Scenario;
import ru.cqa.scenario.definition.model.ScenarioMetadata;
import ru.cqa.scenario.definition.model.Step;
import ru.cqa.scenario.definition.model.move.HttpMove;
import ru.cqa.scenario.definition.model.move.KafkaMove;
import ru.cqa.scenario.definition.model.move.SoapMove;
import ru.cqa.scenario.definition.model.move.SqlMove;
import ru.cqa.scenario.serialization.ScenarioParser;

/** Jackson-адаптер: поле type выбирает конкретную модель Move. */
@Component
public final class JacksonScenarioParser implements ScenarioParser {
    private final JsonMapper mapper;

    public JacksonScenarioParser(JsonMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Scenario parse(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            if (root == null || !root.isObject()) {
                throw new IllegalArgumentException("Корень сценария должен быть JSON-объектом");
            }

            JsonNode metadataNode = root.get("metadata");
            ScenarioMetadata metadata = metadataNode == null || metadataNode.isNull()
                    ? null
                    : mapper.treeToValue(metadataNode, ScenarioMetadata.class);
            List<Dependency> dependencies = readObjects(root.get("dependencies"), Dependency.class);
            Execution execution = readExecution(root.get("execution"));
            List<Step> steps = readSteps(root.get("steps"));

            JsonNode idNode = root.get("id");
            UUID id = idNode == null || idNode.isNull() ? null : UUID.fromString(stringValue(idNode, "id"));
            return new Scenario(id, optionalString(root, "version"), metadata, dependencies, execution, steps);
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
                case "http" -> mapper.treeToValue(properties, HttpMove.class);
                case "sql" -> mapper.treeToValue(properties, SqlMove.class);
                case "kafka" -> mapper.treeToValue(properties, KafkaMove.class);
                case "soap" -> mapper.treeToValue(properties, SoapMove.class);
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
            values.add(mapper.treeToValue(item, type));
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
