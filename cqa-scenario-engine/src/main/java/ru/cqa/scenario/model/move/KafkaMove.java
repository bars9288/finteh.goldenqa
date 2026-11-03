package ru.cqa.scenario.model.move;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.cqa.scenario.model.Assertion;
import ru.cqa.scenario.model.Extract;
import ru.cqa.scenario.model.Move;

import java.util.List;

@Schema(description = "Операция публикации сообщения в Kafka")
public record KafkaMove(

        @Schema(description = "Идентификатор операции", example = "move-003", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String id,

        @Schema(description = "Идентификатор настроенного Kafka-подключения", example = "test-kafka-connection", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String connectionId,

        @Schema(description = "Имя Kafka-топика", example = "user-events", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String topic,

        @Schema(description = "Ключ Kafka-сообщения", example = "123")
        String key,

        @Schema(description = "Тело Kafka-сообщения", example = "{\"userId\":123,\"event\":\"USER_CHECKED\"}")
        String payload,

        @Schema(description = "Проверки результата HTTP-запроса")
        List<@NotNull @Valid Assertion> assertions,

        @Schema(description = "Правила извлечения данных из результата HTTP-запроса")
        List<@NotNull @Valid Extract> extracts ) implements Move {

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return "kafka";
    }

    @Override
    public List<Assertion> assertions() {
        return assertions;
    }

    @Override
    public List<Extract> extracts() {
        return extracts;
    }
}