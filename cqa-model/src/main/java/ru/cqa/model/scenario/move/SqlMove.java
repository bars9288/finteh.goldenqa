package ru.cqa.model.scenario.move;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import ru.cqa.model.scenario.Assertion;
import ru.cqa.model.scenario.Extract;
import ru.cqa.model.scenario.Move;

@Schema(description = "SQL-операция с внешней базой данных")
public record SqlMove(

        @Schema(description = "Идентификатор операции", example = "move-002", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String id,

        @Schema(description = "Идентификатор настроенного подключения к базе данных", example = "test-db-connection", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String connectionId,

        @Schema(description = "SQL-выражение для выполнения", example = "SELECT id, name FROM users WHERE id = 123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String statement,

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
        return "sql";
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
