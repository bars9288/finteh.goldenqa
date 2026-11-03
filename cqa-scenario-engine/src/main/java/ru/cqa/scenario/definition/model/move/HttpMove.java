
package ru.cqa.scenario.definition.model.move;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.cqa.scenario.definition.model.Assertion;
import ru.cqa.scenario.definition.model.Extract;
import ru.cqa.scenario.definition.model.Move;

@Schema(description = "HTTP-запрос к внешнему сервису")
public record HttpMove(

        @Schema(description = "Идентификатор операции", example = "move-001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String id,

        @Schema(description = "Идентификатор настроенного HTTP-подключения", example = "mdm-rest-api-connection", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String connectionId,

        @Schema(description = "HTTP-метод", example = "GET", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String method,

        @Schema(description = "Относительный путь HTTP-запроса", example = "/api/users/123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String path,

        @Schema(description = "HTTP-заголовки запроса в формате ключ-значение", example = "{\"Content-Type\":\"application/json\",\"Authorization\":\"Bearer token\"}")
        Map<String, String> headers,

        @Schema(description = "Тело HTTP-запроса", example = "{\"userId\":123}")
        String requestBody,

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
        return "http";
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
