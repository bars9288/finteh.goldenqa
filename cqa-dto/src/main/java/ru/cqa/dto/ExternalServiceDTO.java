package ru.cqa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import ru.cqa.business.greeting.externalservice.ExternalService;


@Schema(description = "Внешний сервис, участвующий в сценарии")
public record ExternalServiceDTO(

        @Schema(description = "Имя сервиса", example = "KAFKA-CLIENT", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Не должно быть пустым")
        String typeService,

        @Schema(description = "Контур", example = "DEV", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Не должно быть пустым")
        String scope) {

    public ExternalService toExternalService() {
        return new ExternalService(typeService, scope);
    }
}
