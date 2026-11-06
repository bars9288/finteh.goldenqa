package ru.cqa.cases.greeting.externalservice;

import ru.cqa.model.greeting.ExternalServiceDTO;
import ru.cqa.model.greeting.soap.ExternalServiceSoapDTO;

/** DTO внешнего сервиса (REST и SOAP) → доменный {@link ExternalService}. */
public final class ExternalServiceMapper {

    private ExternalServiceMapper() {
    }

    public static ExternalService from(ExternalServiceDTO request) {
        return new ExternalService(request.typeService(), request.scope());
    }

    public static ExternalService from(ExternalServiceSoapDTO request) {
        return new ExternalService(request.getTypeService(), request.getScope());
    }
}
