package ru.cqa.model.greeting;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Ответ ручек приветствия (/hello, /helloService).
 *
 * <p>Текст завёрнут в объект, а не отдан голой строкой: так ответ остаётся
 * расширяемым — новое поле можно добавить, не ломая клиентов.
 */
public record HelloResponseDTO(

        @Schema(description = "Текст приветствия")
        String message) {
}
