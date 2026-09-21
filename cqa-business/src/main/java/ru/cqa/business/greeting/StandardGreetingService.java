package ru.cqa.business.greeting;

/**
 * Приветствие от имени приложения «Единый тестировщик».
 *
 * <p>Обычный Java-класс без аннотаций: модуль не знает про Spring. Бином его
 * делает конфигурация в модуле cqa-server.
 */
public class StandardGreetingService implements GreetingService {

    private static final String TEMPLATE = "Привет из приложения \"Единый тестировщик\" оператору с ФИ = ";

    @Override
    public String greet(Operator operator) {
        if (operator == null) {
            throw new IllegalArgumentException("Оператор не задан");
        }
        return TEMPLATE + operator.shortName();
    }
}
