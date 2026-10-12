package ru.cqa.server.db.sqlite.journal;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;

/**
 * Точечные обновления journal_request — без чтения строки целиком.
 *
 * <p>Наследуется от пустого {@link Repository}, а не от {@code CrudRepository}:
 * его {@code save()} для записи с уже заданным id делает UPDATE, а не INSERT.
 * Вставку делает {@link JournalWriter} через {@code JdbcAggregateTemplate.insert()}.
 *
 * <p>Методы {@code update*} возвращают число изменённых строк.
 */
interface JournalRequestRepository extends Repository<JournalRequest, UUID> {

    Optional<JournalRequest> findById(UUID id);

    @Modifying
    @Query("UPDATE journal_request SET validation_status = :status, validation_errors = :errors WHERE id = :id")
    int updateValidation(UUID id, ValidationStatus status, String errors);

    @Modifying
    @Query("UPDATE journal_request SET internal_error = :error WHERE id = :id")
    int updateInternalError(UUID id, String error);

    @Modifying
    @Query("""
            UPDATE journal_request
               SET finished_at = :finishedAt,
                   resp_status = :status,
                   resp_headers = :headers,
                   resp_body = :body,
                   delivered = :delivered,
                   delivery_error = :deliveryError
             WHERE id = :id""")
    int updateResponse(UUID id, Instant finishedAt, Integer status, String headers, String body,
                       boolean delivered, String deliveryError);
}
