package ru.cqa.model.scenario.move;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import ru.cqa.model.scenario.Assertion;
import ru.cqa.model.scenario.Extract;
import ru.cqa.model.scenario.Move;

@Schema(description = "SOAP-запрос к внешнему сервису")
public record SoapMove(

        @Schema(description = "Идентификатор операции", example = "move-004", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String id,

        @Schema(description = "Идентификатор настроенного SOAP-подключения", example = "test-soap-connection", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String connectionId,

        @Schema(description = "SOAP Action операции", example = "urn:GetUser", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String action,

        @Schema(description = "SOAP Envelope запроса", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String envelope,

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
        return "soap";
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
