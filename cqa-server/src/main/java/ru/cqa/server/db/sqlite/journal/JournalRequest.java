package ru.cqa.server.db.sqlite.journal;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Строка таблицы journal_request: входящий запрос к ЕТ и итоговый ответ клиенту.
 *
 * <p>Колонки — те же имена в snake_case (receivedAt → received_at), так
 * сопоставляет Spring Data JDBC по умолчанию. Поля с JSON (заголовки, ошибки) —
 * готовые строки: коллекцию Spring Data счёл бы связью с другой таблицей.
 *
 * <p>Пишется в два приёма: вставка при приходе запроса, затем точечные
 * обновления — см. {@link JournalWriter}.
 */
@Table("journal_request")
public record JournalRequest(
        @Id UUID id,
        Instant receivedAt,
        Instant finishedAt,
        String clientIp,
        Integer clientPort,
        String httpMethod,
        String endpoint,
        String queryString,
        String reqHeaders,
        String reqContentType,
        BodyKind reqBodyKind,
        String reqBody,
        ValidationStatus validationStatus,
        String validationErrors,
        String internalError,
        Integer respStatus,
        String respHeaders,
        String respBody,
        Boolean delivered,
        String deliveryError) {
}
