package ru.cqa.server.web.greeting;

/**
 * Тело POST-запроса к /helloAkBars.
 *
 * <p>DTO транспортного слоя: поля могут прийти пустыми или не прийти вовсе,
 * поэтому здесь нет проверок — за корректность отвечает доменная модель.
 */
public record HelloRequest(String name, String family) {

    /** Значение поля или {@code fallback}, если в теле запроса его не было. */
    public String nameOr(String fallback) {
        return name != null ? name : fallback;
    }

    public String familyOr(String fallback) {
        return family != null ? family : fallback;
    }
}
