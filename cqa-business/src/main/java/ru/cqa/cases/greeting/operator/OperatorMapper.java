package ru.cqa.cases.greeting.operator;

import ru.cqa.model.greeting.OperatorDTO;
import ru.cqa.model.greeting.soap.OperatorSoapDTO;

/** DTO оператора (REST и SOAP) → доменный {@link Operator}. */
public final class OperatorMapper {

    private OperatorMapper() {
    }

    public static Operator from(OperatorDTO request) {
        return new Operator(request.name(), request.family());
    }

    public static Operator from(OperatorSoapDTO request) {
        return new Operator(request.getName(), request.getFamily());
    }
}
