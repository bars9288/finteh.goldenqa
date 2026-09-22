package ru.cqa.business.greeting;

/**
 * Приветствие внешнему сервису.
 *
 * <p>Ровно тот же шаблон, что и {@link OperatorGreetingService}: чтобы добавить
 * третий вид адресата, достаточно создать такой же класс — правок в других
 * файлах не потребуется.
 */
public class ExternalServiceGreetingService implements GreetingService<ExternalService> {

    private static final String TEMPLATE = "Привет из приложения \"Единый тестировщик\" сервису ";

    @Override
    public Class<ExternalService> subjectType() {
        return ExternalService.class;
    }

    @Override
    public String greet(ExternalService service) {
        if (service == null) {
            throw new IllegalArgumentException("Сервис не задан");
        }
        return TEMPLATE + service.typeService() + " на контуре " + service.scope();
    }
}
