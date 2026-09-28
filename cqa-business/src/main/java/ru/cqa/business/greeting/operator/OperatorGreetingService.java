package ru.cqa.business.greeting.operator;

import ru.cqa.business.greeting.GreetingService;

/**
 * Приветствие оператору.
 *
 * <p>Обычный Java-класс без аннотаций: модуль не знает про Spring.
 * Бином его делает конфигурация в модуле cqa-server.
 */
public class OperatorGreetingService implements GreetingService<Operator> {

    private static final String TEMPLATE = "Привет из приложения \"Единый тестировщик\" оператору с ФИ = ";

    @Override
    public Class<Operator> subjectType() {
        return Operator.class;
    }

    @Override
    public String greet(Operator operator) {
        if (operator == null) {
            throw new IllegalArgumentException("Оператор не задан");
        }
        return TEMPLATE + operator.shortName();
    }
}
