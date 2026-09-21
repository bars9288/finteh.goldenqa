package ru.cqa.server.web.greeting;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Ответ ручки /helloAkBars.
 *
 * <p>Текст завёрнут в объект, а не отдан голой строкой: так ответ остаётся
 * расширяемым — новое поле можно добавить, не ломая клиентов.
 */
public record HelloResponse(@Schema(description = "Текст приветствия") String message) {
}
