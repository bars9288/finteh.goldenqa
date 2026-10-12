package ru.cqa.server.db.sqlite.journal;

/** Итог проверки запроса в журнале; в БД хранится имя константы. */
public enum ValidationStatus {

    /** Ошибок нет. */
    PASSED,

    /** Есть ошибки — они в validation_errors. */
    FAILED,

    /** Проверка не выполнялась: запрос ещё обрабатывается или упал до неё. */
    NOT_RUN
}
