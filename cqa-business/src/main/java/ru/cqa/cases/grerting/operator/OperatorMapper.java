package ru.cqa.cases.grerting.operator;

import ru.cqa.dto.OperatorDTO;
import ru.cqa.dto.soap.OperatorSoapDTO;

/** DTO оператора (REST и SOAP) → доменный {@link Operator}. */
public final class OperatorMapper {

    private OperatorMapper() {
    }

    public static Operator from(OperatorDTO dto) {
        return new Operator(dto.name(), dto.family());
    }

    public static Operator from(OperatorSoapDTO dto) {
        return new Operator(dto.getName(), dto.getFamily());
    }
}
