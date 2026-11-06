package ru.cqa.cases.greeting;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Реестр приветствий: по типу адресата находит реализацию, которая его обслуживает. */
public final class GreetingRegistry {

    private final Map<Class<?>, GreetingService<?>> servicesBySubjectType;

    public GreetingRegistry(Collection<? extends GreetingService<?>> services) {
        if (services == null || services.isEmpty()) {
            throw new IllegalArgumentException("Реестр приветствий пуст: не передано ни одной реализации");
        }
        Map<Class<?>, GreetingService<?>> registeredServices = new LinkedHashMap<>();
        for (GreetingService<?> service : services) {
            GreetingService<?> existingService = registeredServices.put(service.subjectType(), service);
            if (existingService != null) {
                throw new IllegalStateException("Для типа " + service.subjectType().getSimpleName()
                        + " объявлено две реализации: " + existingService.getClass().getSimpleName()
                        + " и " + service.getClass().getSimpleName());
            }
        }
        this.servicesBySubjectType = Map.copyOf(registeredServices);
    }

    public <T> String greet(T subject) {
        if (subject == null) {
            throw new IllegalArgumentException("Адресат не задан");
        }
        @SuppressWarnings("unchecked") // ключ карты — ровно тот класс, который вернул subjectType()
        GreetingService<T> service = (GreetingService<T>) servicesBySubjectType.get(subject.getClass());
        if (service == null) {
            throw new IllegalArgumentException("Нет приветствия для типа " + subject.getClass().getSimpleName());
        }
        return service.greet(subject);
    }

    public Set<Class<?>> supportedTypes() {
        return servicesBySubjectType.keySet();
    }
}
