package ru.cqa.cases.validation.constants;

/**
 * Адреса REST-ручек в одном месте.
 *
 * <p>Константы, а не значения из application.yml: путь в {@code @RequestMapping}
 * должен быть известен на этапе компиляции.
 */
public final class ApiPaths {

    public static final String SAVE_AND_MERGE = "cdi/soap/services/2_13/PartyRA/saveAndMerge";
    public static final String GREETING_OPERATOR = "/hello";
    public static final String GREETING_SERVICE = "/helloService";

    private ApiPaths() {
    }
}
