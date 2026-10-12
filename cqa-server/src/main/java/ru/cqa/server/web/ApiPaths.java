package ru.cqa.server.web;

/**
 * Адреса REST-ручек в одном месте.
 *
 * <p>Константы, а не значения из application.yml: путь в {@code @RequestMapping}
 * должен быть известен на этапе компиляции.
 */
public final class ApiPaths {

    public static final String SAVE_AND_MERGE = "cdi/soap/services/2_13/PartyRA/saveAndMerge";

    private ApiPaths() {
    }
}
