package ru.cqa.cases.grerting.operator;

import ru.cqa.cases.grerting.GreetingService;

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
