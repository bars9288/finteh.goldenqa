package ru.cqa.business.greeting;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Реестр приветствий: по типу адресата находит реализацию, которая его обслуживает.
 *
 * <p>Это та часть шаблона, ради которой заводится дженерик. Вызывающий код не
 * знает, сколько есть реализаций и какие именно, — он передаёт объект и получает
 * результат. Ни одного {@code if} и ни одного {@code instanceof}.
 *
 * <p>Класс из доменного модуля: обычный Java, собрать и протестировать его можно
 * без Spring. В приложении список реализаций приносит контейнер, но для реестра
 * это просто коллекция в конструкторе.
 */
public final class GreetingRegistry {

    private final Map<Class<?>, GreetingService<?>> byType;

    public GreetingRegistry(Collection<? extends GreetingService<?>> services) {
        if (services == null || services.isEmpty()) {
            throw new IllegalArgumentException("Реестр приветствий пуст: не передано ни одной реализации");
        }
        Map<Class<?>, GreetingService<?>> map = new LinkedHashMap<>();
        for (GreetingService<?> service : services) {
            GreetingService<?> previous = map.put(service.subjectType(), service);
            if (previous != null) {
                throw new IllegalStateException("Для типа " + service.subjectType().getSimpleName()
                        + " объявлено две реализации: " + previous.getClass().getSimpleName()
                        + " и " + service.getClass().getSimpleName());
            }
        }
        this.byType = Map.copyOf(map);
    }

    /**
     * Приветствует адресата реализацией, зарегистрированной на его тип.
     *
     * @throws IllegalArgumentException если для типа адресата реализации нет
     */
    public <T> String greet(T subject) {
        if (subject == null) {
            throw new IllegalArgumentException("Адресат не задан");
        }
        @SuppressWarnings("unchecked") // ключ карты — ровно тот класс, который вернул subjectType()
        GreetingService<T> service = (GreetingService<T>) byType.get(subject.getClass());
        if (service == null) {
            throw new IllegalArgumentException("Нет приветствия для типа " + subject.getClass().getSimpleName());
        }
        return service.greet(subject);
    }

    /** Типы, которые реестр умеет обслуживать, — удобно вывести в лог при старте. */
    public Set<Class<?>> supportedTypes() {
        return byType.keySet();
    }
}
