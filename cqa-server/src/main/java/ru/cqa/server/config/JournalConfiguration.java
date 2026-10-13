package ru.cqa.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import ru.cqa.server.db.sqlite.SqliteConverters;
import ru.cqa.server.db.sqlite.SqliteDialect;

/**
 * Настройка Spring Data JDBC под SQLite (БД журнала запросов).
 *
 * <p>Схему создаёт {@code JournalSchema} — скрипт db/schema.sql при старте.
 * Запись в журнал — {@link ru.cqa.server.db.sqlite.journal.JournalWriter}.
 *
 * <p>Оба бина заменяют одноимённые бины Spring Boot: те помечены {@code @ConditionalOnMissingBean}.
 */
@Configuration
public class JournalConfiguration {

    /** Диалект задан явно: сам Spring Data JDBC SQLite не распознаёт. */
    @Bean
    public JdbcDialect jdbcDialect() {
        return SqliteDialect.INSTANCE;
    }

    /** Время и UUID ↔ текст: своих типов для них у SQLite нет. */
    @Bean
    public JdbcCustomConversions jdbcCustomConversions(JdbcDialect dialect) {
        return JdbcCustomConversions.of(dialect, SqliteConverters.all());
    }
}
