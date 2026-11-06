package ru.cqa.scenario.runtime.expression;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import ru.cqa.model.execution.HttpResponseData;

/** Читает базовые JSONPath-выражения по телу JSON-ответа и HTTP-статус из HttpResponseData. */
public final class JsonPathReader {
    private JsonPathReader() {
    }

    public static Object read(Object value, String expression) {
        if (expression == null || expression.isBlank() || expression.charAt(0) != '$') {
            throw new IllegalArgumentException("JSONPath должен начинаться с '$'");
        }

        if (value instanceof HttpResponseData response && "$.statusCode".equals(expression)) {
            return response.statusCode();
        }
        Object currentValue = value instanceof HttpResponseData response ? response.body() : value;
        int index = 1;
        while (index < expression.length()) {
            char marker = expression.charAt(index);
            if (marker == '.') {
                int propertyStartIndex = ++index;
                while (index < expression.length()
                        && expression.charAt(index) != '.'
                        && expression.charAt(index) != '[') {
                    index++;
                }
                if (propertyStartIndex == index) {
                    throw new IllegalArgumentException("Пустое имя свойства в JSONPath: " + expression);
                }
                currentValue = property(currentValue, expression.substring(propertyStartIndex, index), expression);
            } else if (marker == '[') {
                int closingBracketIndex = expression.indexOf(']', index + 1);
                if (closingBracketIndex < 0) {
                    throw new IllegalArgumentException("Не закрыт индекс в JSONPath: " + expression);
                }
                int itemIndex;
                try {
                    itemIndex = Integer.parseInt(expression.substring(index + 1, closingBracketIndex));
                } catch (NumberFormatException exception) {
                    throw new IllegalArgumentException("Поддерживаются только числовые индексы: " + expression);
                }
                currentValue = item(currentValue, itemIndex, expression);
                index = closingBracketIndex + 1;
            } else {
                throw new IllegalArgumentException("Ожидался '.' или '[' в JSONPath: " + expression);
            }
        }
        return currentValue;
    }

    private static Object property(Object value, String name, String expression) {
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Свойство '" + name + "' не найдено в " + expression);
        }
        if (!map.containsKey(name)) {
            throw new IllegalArgumentException("Свойство '" + name + "' не найдено в " + expression);
        }
        return map.get(name);
    }

    private static Object item(Object value, int index, String expression) {
        if (value instanceof List<?> list && index >= 0 && index < list.size()) {
            return list.get(index);
        }
        if (value != null && value.getClass().isArray() && index >= 0 && index < Array.getLength(value)) {
            return Array.get(value, index);
        }
        throw new IllegalArgumentException("Индекс " + index + " не найден в " + expression);
    }
}
