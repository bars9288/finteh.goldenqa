package ru.cqa.scenario.definition.model.move;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.cqa.scenario.definition.model.Assertion;
import ru.cqa.scenario.definition.model.Extract;
import ru.cqa.scenario.definition.model.Move;

import java.util.List;

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