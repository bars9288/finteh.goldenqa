package ru.cqa.scenario.serialization;

import java.util.Map;
import ru.cqa.scenario.model.Move;

/** Создаёт типизированную модель Move из JSON-полей без привязки к JSON-библиотеке. */
public interface MoveDeserializer {
    Move deserialize(String type, Map<String, Object> properties);
}
