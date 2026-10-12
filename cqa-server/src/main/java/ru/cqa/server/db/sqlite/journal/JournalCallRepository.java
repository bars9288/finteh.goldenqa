package ru.cqa.server.db.sqlite.journal;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;

/** Точечные обновления journal_call; почему не {@code CrudRepository} — см. {@link JournalRequestRepository}. */
interface JournalCallRepository extends Repository<JournalCall, UUID> {

    Optional<JournalCall> findById(UUID id);

    /** Запрос по имени метода: Spring Data строит SQL сам. */
    List<JournalCall> findByRequestIdOrderBySentAt(UUID requestId);

    @Modifying
    @Query("""
            UPDATE journal_call
               SET finished_at = :finishedAt,
                   resp_status = :status,
                   resp_headers = :headers,
                   resp_content_type = :contentType,
                   resp_body_kind = :bodyKind,
                   resp_body = :body
             WHERE id = :id""")
    int updateAnswer(UUID id, Instant finishedAt, Integer status, String headers, String contentType,
                     BodyKind bodyKind, String body);

    @Modifying
    @Query("UPDATE journal_call SET finished_at = :finishedAt, call_error = :error WHERE id = :id")
    int updateError(UUID id, Instant finishedAt, String error);
}
