package ru.cqa.business.greeting;

/**
 * Оператор, от имени которого выполняется проверка.
 *
 * <p>Модель сама следит за своей корректностью: собрать объект с пустым именем
 * или фамилией нельзя. Поэтому дальше по коду проверять их уже не нужно —
 * если экземпляр существует, значит он валиден.
 */
public record Operator(String name, String family) {

    public Operator {
        name = requireFilled(name, "name");
        family = requireFilled(family, "family");
    }

    private static String requireFilled(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Поле '" + field + "' обязательно и не может быть пустым");
        }
        return value.strip();
    }

    /** Фамилия и имя одной строкой — в том порядке, в котором их ждёт оператор. */
    public String shortName() {
        return name + " " + family;
    }
}
