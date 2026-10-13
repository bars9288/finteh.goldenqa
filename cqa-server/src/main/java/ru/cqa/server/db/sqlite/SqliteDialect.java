package ru.cqa.server.db.sqlite;

import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.relational.core.dialect.AbstractDialect;
import org.springframework.data.relational.core.dialect.LimitClause;
import org.springframework.data.relational.core.dialect.LockClause;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.data.relational.core.sql.LockOptions;

/**
 * Диалект SQLite для Spring Data JDBC.
 *
 * <p>Своего диалекта SQLite в Spring Data JDBC нет: по соединению он узнаёт только
 * PostgreSQL, MySQL, H2 и ещё несколько БД, а на остальных падает при старте
 * с {@code NoDialectException}. Здесь описано то немногое, чем SQL разных БД
 * отличается для Spring Data: постраничная выборка, блокировки и имена.
 */
public class SqliteDialect extends AbstractDialect implements JdbcDialect {

    public static final SqliteDialect INSTANCE = new SqliteDialect();

    /** {@code LIMIT n OFFSET m}; OFFSET без LIMIT SQLite не принимает — отсюда {@code LIMIT -1}. */
    private static final LimitClause LIMIT_CLAUSE = new LimitClause() {

        @Override
        public String getLimit(long limit) {
            return "LIMIT " + limit;
        }

        @Override
        public String getOffset(long offset) {
            return "LIMIT -1 OFFSET " + offset;
        }

        @Override
        public String getLimitOffset(long limit, long offset) {
            return "LIMIT " + limit + " OFFSET " + offset;
        }

        @Override
        public Position getClausePosition() {
            return Position.AFTER_ORDER_BY;
        }
    };

    /** Блокировок строк ({@code FOR UPDATE}) в SQLite нет: запись блокирует всю БД сама. */
    private static final LockClause LOCK_CLAUSE = new LockClause() {

        @Override
        public String getLock(LockOptions lockOptions) {
            return "";
        }

        @Override
        public Position getClausePosition() {
            return Position.AFTER_ORDER_BY;
        }
    };

    @Override
    public LimitClause limit() {
        return LIMIT_CLAUSE;
    }

    @Override
    public LockClause lock() {
        return LOCK_CLAUSE;
    }

    /** Имена в кавычках как есть: таблицы в schema.sql в нижнем регистре. */
    @Override
    public IdentifierProcessing getIdentifierProcessing() {
        return IdentifierProcessing.create(IdentifierProcessing.Quoting.ANSI, IdentifierProcessing.LetterCasing.AS_IS);
    }
}
