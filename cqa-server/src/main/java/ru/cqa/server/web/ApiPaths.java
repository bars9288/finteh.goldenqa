package ru.cqa.server.web;

/**
 * Адреса REST-ручек в одном месте.
 *
 * <p>Здесь только плейсхолдеры: сами адреса — в connections.yml ({@code cqa.api.paths.*}),
 * переопределяются переменными окружения или .env. Spring подставляет их в {@code @RequestMapping} при старте.
 */
public final class ApiPaths {

    public static final String SAVE_AND_MERGE    = "${cqa.api.paths.save-and-merge}";
    public static final String GREETING_OPERATOR = "${cqa.api.paths.greeting-operator}";
    public static final String GREETING_SERVICE  = "${cqa.api.paths.greeting-service}";
    public static final String ECHO              = "${cqa.api.paths.echo}";
    public static final String SCENARIOS          = "${cqa.api.paths.scenarios}";

    private ApiPaths() {
    }
}
