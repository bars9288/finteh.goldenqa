package ru.cqa.cases.greeting.externalservice;

import ru.cqa.dto.ExternalServiceDTO;
import ru.cqa.dto.soap.ExternalServiceSoapDTO;

/** DTO внешнего сервиса (REST и SOAP) → доменный {@link ExternalService}. */
public final class ExternalServiceMapper {

    private ExternalServiceMapper() {
    }

    public static ExternalService from(ExternalServiceDTO dto) {
        return new ExternalService(dto.typeService(), dto.scope());
    }

    public static ExternalService from(ExternalServiceSoapDTO dto) {
        return new ExternalService(dto.getTypeService(), dto.getScope());
    }
}
