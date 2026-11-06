package ru.cqa.server.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

/** Явное подключение веб-адаптеров и репозиториев независимо от пакета main-класса. */
@Configuration
@ComponentScan("ru.cqa.server")
@EnableJdbcRepositories("ru.cqa.server.db.sqlite.journal")
public class ServerConfiguration {
}
