package ru.cqa.server.web.greeting;

/**
 * Ответ ручки /helloAkBars.
 *
 * <p>Текст завёрнут в объект, а не отдан голой строкой: так ответ остаётся
 * расширяемым — новое поле можно добавить, не ломая клиентов.
 */
public record HelloResponse(String message) {
}
