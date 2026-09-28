package ru.cqa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import ru.cqa.business.greeting.externalservice.ExternalService;

/**
 * Тело POST-запроса к /helloService.
 *
 * <p>Проверяет только форму запроса: оба поля пришли и не пустые. Бизнес-правила
 * остаются в {@link ExternalService} — его конструктор проверяет данные
 * независимо от того, откуда они пришли.
 */
@Schema(description = "Внешний сервис, участвующий в сценарии")
public record ExternalServiceDTO(
        @Schema(description = "Имя сервиса", example = "KAFKA-CLIENT", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String typeService,
        @Schema(description = "Контур", example = "DEV", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String scope) {

    /** Доменный объект из тела запроса. */
    public ExternalService toExternalService() {
        return new ExternalService(typeService, scope);
    }
}
