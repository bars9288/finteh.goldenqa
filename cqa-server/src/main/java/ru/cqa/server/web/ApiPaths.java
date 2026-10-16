package ru.cqa.server.web;

/**
 * Адреса REST-ручек в одном месте.
 *
 * <p>Константы, а не значения из application.yml: путь в {@code @RequestMapping}
 * должен быть известен на этапе компиляции.
 */
public final class ApiPaths {

    public static final String SAVE_AND_MERGE = "cdi/soap/services/2_13/PartyRA/saveAndMerge";

    /** Чтение журнала запросов — служебная ручка веб-интерфейса, сама в журнал не пишется. */
    public static final String JOURNAL = "/api/journal";

    /** Сводка о приложении — служебная ручка веб-интерфейса, сама в журнал не пишется. */
    public static final String INFO = "/api/info";

    /** Веб-интерфейс: статика из classpath:/static/ui/. */
    public static final String UI = "/ui";

    private ApiPaths() {
    }
}
