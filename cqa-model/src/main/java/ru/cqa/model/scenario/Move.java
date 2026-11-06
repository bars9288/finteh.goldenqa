
package ru.cqa.model.scenario;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import ru.cqa.model.scenario.move.HttpMove;
import ru.cqa.model.scenario.move.KafkaMove;
import ru.cqa.model.scenario.move.SoapMove;
import ru.cqa.model.scenario.move.SqlMove;

@Schema(
        description = "Операция, выполняемая в рамках шага сценария",
        oneOf = {
                HttpMove.class,
                SqlMove.class,
                KafkaMove.class,
                SoapMove.class
        },
        discriminatorProperty = "type",
        discriminatorMapping = {
                @DiscriminatorMapping(value = "http", schema = HttpMove.class),
                @DiscriminatorMapping(value = "sql", schema = SqlMove.class),
                @DiscriminatorMapping(value = "kafka", schema = KafkaMove.class),
                @DiscriminatorMapping(value = "soap", schema = SoapMove.class)
        }
)
public interface Move {

    @Schema(description = "Уникальный идентификатор операции", example = "move-001", requiredMode = Schema.RequiredMode.REQUIRED)
    String getId();

    @Schema(description = "Тип операции", example = "http", requiredMode = Schema.RequiredMode.REQUIRED)
    String getType();

    @Schema(description = "Проверки результата операции")
    List<Assertion> assertions();

    @Schema(description = "Правила извлечения данных из результата операции")
    List<Extract> extracts();
}
