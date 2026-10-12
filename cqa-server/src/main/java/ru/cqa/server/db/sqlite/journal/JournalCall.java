package ru.cqa.server.db.sqlite.journal;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Строка таблицы journal_call: исходящий вызов внешнего сервиса (МДМ).
 *
 * <p>Отдельный агрегат, а не часть {@link JournalRequest}: иначе Spring Data JDBC
 * при каждом обновлении запроса удалял бы и заново вставлял все его вызовы.
 * Связь — просто идентификатор запроса.
 */
@Table("journal_call")
public record JournalCall(
        @Id UUID id,
        UUID requestId,
        String service,
        Instant sentAt,
        Instant finishedAt,
        String targetUrl,
        String reqHeaders,
        Integer respStatus,
        String respHeaders,
        String respContentType,
        BodyKind respBodyKind,
        String respBody,
        String callError) {
}
