package ru.cqa.business.greeting;

/**
 * Порт: формирует приветствие оператору.
 *
 * <p>Интерфейс объявлен в доменном модуле, а веб-слой зависит только от него —
 * так стрелка зависимости смотрит внутрь, к домену, а не наружу.
 */
@FunctionalInterface
public interface GreetingService {

    /**
     * @param operator оператор, которому адресовано приветствие
     * @return текст приветствия
     */
    String greet(Operator operator);
}
